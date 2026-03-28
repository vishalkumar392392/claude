# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Shopline** — a React + Vite SPA used as a course project demonstrating Claude Code's multi-agent orchestration workflow for implementing user stories end-to-end.

## Commands

```bash
npm install       # Install dependencies
npm run dev       # Start dev server with HMR (http://localhost:5173)
npm run build     # Production bundle → /dist
npm run preview   # Preview production build
npm run lint      # Run ESLint
```

## Multi-Agent Workflow

The core of this project is a three-stage agent pipeline invoked via `/implement-story`:

1. **ux-design-planner** (`.claude/agents/ux-design-planner.md`) — converts a user story into a written UI/UX spec (no code). Outputs component list, layout description, and interaction flows.
2. **General-purpose coding agent** — implements the spec from stage 1.
3. **playwright-feature-verifier** (`.claude/agents/playwright-feature-verifier.md`) — connects to the running app, interacts with new features, and saves screenshots to `test-output/`.

The orchestration command is defined in `.claude/commands/implement-story.md`. Agents pass their output forward: each stage's result becomes the next stage's input.

### Agent Memory

Agents persist institutional knowledge in `.claude/agent-memory/`. The verifier expects the app running at `http://127.0.0.1:5000` — start `npm run dev` (or configure the port) before verification runs.

## Architecture

- **Entry:** `index.html` → `src/main.jsx` → `src/App.jsx`
- **Styling:** CSS custom properties for theming (light/dark via `prefers-color-scheme`), mobile breakpoint at 1024px
- **Build:** Vite with `@vitejs/plugin-react`; ES modules throughout (`"type": "module"`)
- **Linting:** ESLint targets `*.{js,jsx}`, ES2020, browser globals; uppercase/underscore-prefixed vars are exempt from `no-unused-vars`
