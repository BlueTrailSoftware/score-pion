---
name: Frontend UI Standards & Design System
description: Strict guidelines for building consistent UI components, forms, tables, modals, and actions across the frontend application. Always consult this before touching frontend layout or styles.
---

# UI Standards and Components

When working on the frontend of this application (Score-Pion Web), ALWAYS adhere to the following established UI components and globally centralized styles. Do not "improvise" local `.scss` styles or recreate existing visual patterns manually.

## 1. Global Styling Architecture & Variables
**Problem:** Inconsistent colors and outdated SCSS syntax leading to compilation warnings.
**Solution:** A centralized architecture in `src/app/styles/`.
- **`_theme-variables.scss`**: The SINGLE source of truth for all colors, radii, and shadows (`$sp-gradient-start`, `$sp-danger`, `$sp-shadow-card`). Use `@use './app/styles/theme-variables' as tv;` to access them. **Hardcoding hex colors in `.scss` fields is strictly prohibited.**
- **Dart Sass Compliance**: Use modern Dart Sass rules. Replace `@import` with `@use`. Never use `darken()` or `lighten()`; use `color.adjust($color, $lightness: ...)` instead.

## 2. Iconography Standards
Use the following Bootstrap Icons (BI) for consistency across the platform:
- **Trigger Dropdowns**: ALWAYS use `bi-chevron-down`.
- **Edit**: `bi-pencil`
- **Delete / Reject**: `bi-trash` or `bi-x-circle` (prefer `bi-x-circle` for soft rejections)
- **Invite / Add User**: `bi-person-plus`
- **Add / New Item**: `bi-plus-lg` (or `bi-plus-circle` for floating buttons)
- **Activate / Play**: `bi-play-circle` or `bi-person-check`
- **Deactivate / Pause**: `bi-pause-circle` or `bi-person-x`
- **Settings**: `bi-gear-fill`
- **Info**: `bi-info-circle`

## 3. Action Dropdowns (`app-action-dropdown`)
**Problem:** Developers previously built manual Bootstrap dropdowns or hardcoded individual buttons in tables, breaking visual consistency.
**Solution:** ALWAYS use `<app-action-dropdown>` for grouping interactable row/page actions.

### Usage
- **Global Page Actions (`type="gradient"`)**: For main page features. Renders as a wide gradient button with text.
- **Table Row Actions (`type="icon"`)**: For table rows. Renders as a compact, square green gradient button (`38x38px`) with a chevron.

### Disabling Items
Disable items via the `disabled` property in the `ActionItem` object. The dropdown will natively block clicks and show a disabled state.

## 4. Modals & Action Buttons
- **Primary Action (Submit/Create/Confirm):** Must use `class="btn btn-gradient-primary"` (or `btn-danger` for deletions).
- **Secondary Action (Cancel/Close/Back):** MUST ALWAYS use `class="btn btn-outline-secondary"`. White background, grey border, turns grey on hover. **Avoid `btn-secondary` (solid grey).**

## 5. Navigation (Tabs)
- **Component**: Always use `<app-tabs>`.
- **Logic**: Bottom-border style with 3px active line. Supports optional counters. Do not build custom `<ul>` navs.

## 6. Badges & Status Indicators
- **Design**: "Outline Badge" style (pastel border + subtle light background).
- **Logic**: Use the `StatusBadgePipe` (`| statusBadge`) for mapping status strings to CSS classes defined in `_badges.scss`.

## 7. Layout and Tables
- **Main Wrapper**: Wrap the entire page content in a `<div class="sp-page-wrapper">`.
- **Forms Wrapper**: Use `<div class="card squared-form-wrapper">` for structural cards.
- **Tables**: Always apply `.sp-table` to `<table>` elements.
  - Name/ID columns: Left-aligned, `fw-medium`.
  - Action columns: Right-aligned (`text-end`).
- **Filters Row**: Assign fixed widths to fixed selectors (`width: 200px`) and use `flex-grow-1` for the search input.

## 8. Layout Archetypes (AI Starter Anatomies)
Use these structural patterns for consistent page building. AI Agents MUST follow these exact HTML hierarchies.

### A. The Listing Page (Table View)
Structure: Main Container (Background) > Page Wrapper > Header > Card > Filter Row > Table
```html
<div class="sp-main-container">
  <div class="sp-page-wrapper">
    <!-- 1. Unified Header -->
    <header class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h1 class="h3 mb-1">Page Title</h1>
        <p class="text-muted mb-0">Subtitle describing the view</p>
      </div>
      <!-- Global Action (Optional) -->
      <app-action-dropdown type="gradient" [actions]="pageActions"></app-action-dropdown>
    </header>

    <!-- 2. Main Content Card -->
    <div class="card shadow-sm border-0">
      <div class="card-body p-0"> <!-- p-0 for table alignment -->
        
        <!-- 3. Filters/Search Row -->
        <div class="p-3 border-bottom d-flex gap-3 align-items-center">
          <div class="flex-grow-1">
            <input type="text" class="form-control" placeholder="Search...">
          </div>
          <select class="form-select" style="width: 250px;">
            <option>Filter by...</option>
          </select>
        </div>

        <!-- 4. Standard Table -->
        <div class="table-responsive">
          <table class="table sp-table mb-0">
            <thead>
              <tr>
                <th>Name</th>
                <th>Status</th>
                <th class="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td class="fw-medium">John Doe</td>
                <td><span class="badge status-active">Active</span></td>
                <td class="text-end">
                  <app-action-dropdown type="icon" [actions]="rowActions"></app-action-dropdown>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</div>
```

### B. The Modal Dialogue
Structure: Overlay > Modal Content > Standardized Footer
```html
<div class="modal-overlay"> <!-- Global standard backdrop -->
  <div class="modal-dialog modal-dialog-centered">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title">Action Title</h5>
        <button type="button" class="btn-close" (click)="close()"></button>
      </div>
      <div class="modal-body">
        <!-- Content goes here -->
      </div>
      <div class="modal-footer">
        <!-- ALWAYS: Secondary (Cancel) on LEFT or FIRST, Primary (Action) on RIGHT -->
        <button type="button" class="btn btn-outline-secondary" (click)="close()">Cancel</button>
        <button type="button" class="btn btn-gradient-primary">Execute Action</button>
      </div>
    </div>
  </div>
</div>
```
