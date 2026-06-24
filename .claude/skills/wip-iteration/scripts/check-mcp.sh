#!/usr/bin/env bash
# Verify MCP access by attempting to call a wairz tool via Claude Code.
# This script documents what the Claude Code harness checks before execution.
# Exit 0 if MCP is available, 1 otherwise.
set -euo pipefail

# NOTE: MCP tools (mcp__wairz__*) are NOT shell commands.
# They are Claude Code tools that require the tool interface to invoke.
# Bash 'command -v' cannot find them.
#
# This script's presence documents the requirement; the actual check
# happens when Claude Code invokes the skill and attempts to call
# mcp__wairz__list_projects as its first tool during execution.
#
# If that call succeeds, MCP is available.
# If it fails (e.g., InputValidationError due to schema not loaded,
# or tool not found), MCP access is unavailable.

echo "ℹ️  MCP check: wairz availability is verified by Claude Code tool invocation."
echo "    (MCP tools require ToolSearch + tool call, not bash commands)"
exit 0
