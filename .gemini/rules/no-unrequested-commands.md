# No Unrequested Commands Rule

Never add unrequested, speculative, or diagnostic commands (e.g., `/test`, `/status`, `/load`, `/tp`, debug inspection commands, demo CLI handlers) to plugin code or server logic.

## Rules:
1. **Zero Unrequested Commands:** ONLY add a command if the user explicitly requested that specific command by name, syntax, or explicit functional requirement in their prompt.
2. **Never Add "Convenience" / "Inspection" Commands:** Never invent admin commands, inspection commands, or reload/status commands (e.g. `/myplugin status`, `/myplugin load`, `/myplugin tp`) "just in case" or "for convenience".
3. **Rely on Native Platform Tools:** Use standard server logs (`getLogger().info(...)`) for lifecycle status and vanilla commands (`/execute in <dim> run tp`, `/datapack list`) instead of writing custom command boilerplate.
4. **No Speculative Architecture for Commands:** Do not create maps, registries, or manager abstractions whose sole purpose is to serve unrequested commands.
5. **Shortest Working Logic:** If the user asks for an automated system (e.g. auto-loading worlds, registering datapacks, handling events), implement the automated logic directly and stop there. Do not attach a command interface unless asked.
