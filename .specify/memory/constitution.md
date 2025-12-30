<!--
Sync Impact Report:
- Version: Initial creation → 1.0.0
- Ratification: 2025-12-31
- Modified principles: N/A (initial creation)
- Added sections:
  * Core Principles (5 principles: Relationship-First Design, Weekly Rhythm, Autonomous Partnership, Celebration Over Judgment, Intentional Simplicity)
  * Technical Principles (Platform, Architecture, Code Quality, State Management, Data Layer, Dependency Injection, Design System, Performance, Accessibility)
  * Decision Framework
  * Non-Negotiables
- Removed sections: N/A
- Templates requiring updates:
  ✅ plan-template.md - Updated to reference KMP structure and Android builds
  ✅ spec-template.md - Compatible with relationship-first user scenarios
  ✅ tasks-template.md - Compatible with independent story testing approach
- Follow-up TODOs: None
-->

# Tandem Constitution

## Core Principles

### I. Relationship-First Design

Every feature MUST strengthen, not strain, the couple's relationship. The system SHALL NOT enable surveillance, nagging, or control dynamics between partners.

**Rationale**: Productivity tools can inadvertently create tension in relationships through asymmetric visibility or implicit pressure. Tandem's fundamental purpose is to support couples, not optimize individual task completion at the expense of relationship health.

### II. Weekly Rhythm

The week is the atomic unit of time. All features MUST center on the weekly planning → execution → review cycle.

**Rationale**: Daily task management creates cognitive overhead and promotes anxious checking. Monthly cycles lose granularity. The weekly rhythm matches natural human planning horizons and creates sustainable review points.

### III. Autonomous Partnership

Each partner owns their tasks and goals. Visibility is passive. Task requests MUST require acceptance.

**Rationale**: Autonomy prevents resentment. One partner cannot assign tasks to another without consent. Visibility supports awareness without creating obligation or surveillance dynamics.

### IV. Celebration Over Judgment

Frame outcomes positively. Use "Tried" not "Failed". Use "Skipped" not "Abandoned".

**Rationale**: Language shapes perception. Judgmental language in a shared tool creates shame and avoidance. Positive framing encourages honesty and reduces defensive behavior.

### V. Intentional Simplicity

Resist feature creep. NO due dates within weeks. NO priority levels. NO subtasks. NO categories.

**Rationale**: Complexity creates cognitive load and reduces adoption. Each omitted feature is intentional. Constraints force clarity. The weekly rhythm eliminates the need for internal due dates. Flat task lists prevent over-engineering of simple intentions.

## Technical Principles

### Platform

- Kotlin Multiplatform with Compose Multiplatform
- Android-first (Material Design 3), iOS preparation
- Minimum Android SDK: 26 (Android 8.0)

### Architecture

- Clean Architecture with MVI pattern
- Unidirectional data flow
- Domain layer is 100% shared code
- UI layer is platform-specific (Jetpack Compose for Android)

### Code Quality

- Kotlin-first idioms (sealed classes, data classes, extension functions)
- Null safety enforced (no `!!` operators)
- All public functions documented with KDoc
- Unit tests for all use cases and ViewModels
- UI tests for critical flows
- Test if build is successful (`:composeApp:compileDebugKotlinAndroid`) to validate Android implementations

### State Management

- Single source of truth per feature (ViewModel)
- UI State represented as immutable data classes
- Side effects via sealed class events
- Compose state hoisting

### Data Layer

- SQLDelight for local persistence
- Repository pattern with interfaces in domain
- Offline-first with sync queue
- DataStore for preferences

### Dependency Injection

- Koin for DI
- Module per feature
- Constructor injection only

### Design System

- Material Design 3 components
- Dynamic color (Material You) support
- Light and Dark mode required
- Custom theme tokens for brand colors
- 8dp grid system

### Performance

- Lazy loading for lists
- Image caching
- Debounced inputs
- Background sync with WorkManager

### Accessibility

- Content descriptions for all interactive elements
- Minimum touch target 48dp
- Support for TalkBack
- Dynamic text sizing

## Decision Framework

When making technical decisions, evaluate against these criteria:

1. Does it strengthen the weekly rhythm?
2. Does it respect partner autonomy?
3. Is it the simplest solution that works?
4. Can it work offline?
5. Does it follow Material Design 3 patterns?

Decisions MUST pass all five criteria. If a decision fails any criterion, it requires explicit justification and approval.

## Non-Negotiables

The following features are PROHIBITED and SHALL NOT be implemented:

- NO tracking of partner's incomplete tasks
- NO notifications for partner's task completions (default off)
- NO assigning tasks without acceptance workflow
- NO shame language in UI copy
- NO complex task hierarchies

Violations of non-negotiables invalidate implementation plans and MUST be removed.

## Governance

### Amendment Process

1. Proposed amendments MUST be documented with rationale
2. Breaking changes (removing principles, changing non-negotiables) require MAJOR version bump
3. New principles or expanded guidance require MINOR version bump
4. Clarifications and wording improvements require PATCH version bump
5. All amendments MUST update dependent templates (plan-template.md, spec-template.md, tasks-template.md)

### Compliance Review

- All feature specifications MUST pass constitution check before planning
- All implementation plans MUST verify compliance in "Constitution Check" section
- Code reviews MUST verify adherence to technical principles
- Non-negotiables MUST be enforced at design time, not corrected after implementation

### Template Synchronization

When the constitution changes:
1. Update plan-template.md "Constitution Check" section
2. Review spec-template.md for requirement alignment
3. Review tasks-template.md for task categorization alignment
4. Update any agent guidance files referencing specific principles

**Version**: 1.0.0 | **Ratified**: 2025-12-31 | **Last Amended**: 2025-12-31
