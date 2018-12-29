package com.gildedgames.orbis.scripting.impl.lua;

import com.gildedgames.orbis.scripting.IScript;
import com.gildedgames.orbis.scripting.IScriptPars;
import com.gildedgames.orbis.scripting.IScriptingEngine;
import com.gildedgames.orbis.scripting.impl.Script;
import com.gildedgames.orbis.scripting.impl.ScriptPars;
import com.gildedgames.orbis_api.OrbisLib;
import com.google.common.collect.Maps;
import org.luaj.vm2.*;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ScriptingEngineLua implements IScriptingEngine
{
	private static Globals server_globals;

	private String script;

	private IScriptPars pars = new ScriptPars();

	private Map<String, Object> variableToValue = Maps.newHashMap();

	private Globals user_globals;

	public ScriptingEngineLua()
	{
		server_globals = new Globals();
		server_globals.load(new JseBaseLib());
		server_globals.load(new PackageLib());
		server_globals.load(new StringLib());

		server_globals.load(new JseMathLib());
		LoadState.install(server_globals);
		LuaC.install(server_globals);

		// Set up the LuaString metatable to be read-only since it is shared across all scripts.
		LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
	}

	// Run a script in a lua thread and limit it to a certain number
	// of instructions by setting a hook function.
	// Give each script its own copy of globals, but leave out libraries
	// that contain functions that can be abused.
	private Optional<Object> runScriptInSandbox(String script, IScriptPars pars)
	{
		// Each script will have it's own set of globals, which should
		// prevent leakage between scripts running on the same server.
		this.user_globals = new Globals();
		this.user_globals.load(new JseBaseLib());
		this.user_globals.load(new PackageLib());
		this.user_globals.load(new Bit32Lib());
		this.user_globals.load(new TableLib());
		this.user_globals.load(new StringLib());
		this.user_globals.load(new JseMathLib());

		// The debug library must be loaded for hook functions to work, which
		// allow us to limit scripts to run a certain number of instructions at a time.
		// However we don't wish to expose the library in the user globals,
		// so it is immediately removed from the user globals once created.
		this.user_globals.load(new DebugLib());
		LuaValue sethook = this.user_globals.get("debug").get("sethook");
		this.user_globals.set("debug", LuaValue.NIL);

		// Set up the script to run in its own lua thread, which allows us
		// to set a hook function that limits the script to a specific number of cycles.
		// Note that the environment is set to the user globals, even though the
		// compiling is done with the server globals.

		LuaValue chunk = server_globals.load(script, "main", this.user_globals);

		for (Map.Entry<String, Object> entry : pars.getEntries())
		{
			Object val = entry.getValue();

			if (val instanceof LuaValue)
			{
				this.user_globals.set(entry.getKey(), (LuaValue) entry.getValue());
			}
			else if (val instanceof String)
			{
				this.user_globals.set(entry.getKey(), LuaValue.valueOf((String) val));
			}
			else if (val instanceof Integer)
			{
				this.user_globals.set(entry.getKey(), LuaValue.valueOf((Integer) val));
			}
			else if (val instanceof Double)
			{
				this.user_globals.set(entry.getKey(), LuaValue.valueOf((Double) val));
			}
			else if (val instanceof Float)
			{
				this.user_globals.set(entry.getKey(), LuaValue.valueOf((Float) val));
			}
			else if (val instanceof Byte)
			{
				this.user_globals.set(entry.getKey(), LuaValue.valueOf((Byte) val));
			}
			else if (val instanceof byte[])
			{
				this.user_globals.set(entry.getKey(), LuaValue.valueOf((byte[]) val));
			}
			else
			{
				this.user_globals.set(entry.getKey(), CoerceJavaToLua.coerce(val));
			}
		}

		LuaThread thread = new LuaThread(this.user_globals, chunk);

		// Set the hook function to immediately throw an Error, which will not be
		// handled by any Lua code other than the coroutine.
		LuaValue hookfunc = new ZeroArgFunction()
		{
			@Override
			public LuaValue call()
			{
				// A simple lua error may be caught by the script, but a
				// Java Error will pass through to top and stop the script.
				throw new Error("Script overran resource limits.");
			}
		};

		final int instruction_count = 2000;
		sethook.invoke(LuaValue.varargsOf(new LuaValue[] { thread, hookfunc,
				LuaValue.EMPTYSTRING, LuaValue.valueOf(instruction_count) }));

		// When we resume the thread, it will run up to 'instruction_count' instructions
		// then call the hook function which will error out and stop the script.
		Varargs result = thread.resume(LuaValue.NIL);

		if (result.narg() > 1)
		{
			LuaValue value = result.arg(2);

			if (value.isboolean())
			{
				return Optional.of(value.checkboolean());
			}
			else if (value.isint())
			{
				return Optional.of(value.checkint());
			}
			else if (value.islong())
			{
				return Optional.of(value.checklong());
			}
			else if (value.isstring())
			{
				return Optional.of(value.checkjstring());
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<Object> eval(String script)
	{
		this.script = script;

		return this.runScriptInSandbox(script, this.pars);
	}

	@Override
	public Optional<Object> eval(String script, IScriptPars pars)
	{
		this.script = script;

		return this.runScriptInSandbox(script, pars);
	}

	@Override
	public String getExtension()
	{
		return "lua";
	}

	@Override
	public IScript compile(String script)
	{
		return new Script(this, script);
	}

	@Override
	public IScript compile(Reader reader)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			String line;

			BufferedReader br = new BufferedReader(reader);

			while ((line = br.readLine()) != null)
			{
				sb.append(line);
			}

			br.close();

			return new Script(this, sb.toString());
		}
		catch (IOException e)
		{
			OrbisLib.LOGGER.info("Could not read script from reader when compiling", e);
		}

		return null;
	}

	@Override
	public Optional<Object> get(String variable)
	{
		return Optional.ofNullable(this.user_globals.get(variable));
	}

	@Override
	public Collection<Map.Entry<String, Object>> getEntries()
	{
		return this.pars.getEntries();
	}

	@Override
	public void put(String parameter, Object value)
	{
		this.pars.put(parameter, value);
	}

	@Override
	public Optional<Object> getValue(String parameter)
	{
		return this.pars.getValue(parameter);
	}

	// Simple read-only table whose contents are initialized from another table.
	static class ReadOnlyLuaTable extends LuaTable
	{
		public ReadOnlyLuaTable(LuaValue table)
		{
			this.presize(table.length(), 0);
			for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table
					.next(n.arg1()))
			{
				LuaValue key = n.arg1();
				LuaValue value = n.arg(2);
				super.rawset(key, value.istable() ? new ReadOnlyLuaTable(value) : value);
			}
		}

		@Override
		public LuaValue setmetatable(LuaValue metatable)
		{
			return error("table is read-only");
		}

		@Override
		public void set(int key, LuaValue value)
		{
			error("table is read-only");
		}

		@Override
		public void rawset(int key, LuaValue value)
		{
			error("table is read-only");
		}

		@Override
		public void rawset(LuaValue key, LuaValue value)
		{
			error("table is read-only");
		}

		@Override
		public LuaValue remove(int pos)
		{
			return error("table is read-only");
		}
	}
}
