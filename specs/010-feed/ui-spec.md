# Feed Screen - UI Specification

> **Reference Mockup:** `mockups/feed-v1.html`
> **Last Updated:** January 23, 2026
> **Version:** 1.0

This document provides pixel-perfect specifications to reproduce the Feed screen exactly as shown in the mockup.

---

## Table of Contents

1. [Design Tokens](#1-design-tokens)
2. [Screen Layout](#2-screen-layout)
3. [Top Navigation Bar](#3-top-navigation-bar)
4. [Filter Bar](#4-filter-bar)
5. [Section Headers (Sticky Day Headers)](#5-section-headers-sticky-day-headers)
6. [Feed Cards](#6-feed-cards)
7. [Card Variants](#7-card-variants)
8. [Caught Up Separator](#8-caught-up-separator)
9. [Message Input Bar](#9-message-input-bar)
10. [Animations](#10-animations)
11. [Sample Data](#11-sample-data)

---

## 1. Design Tokens

### 1.1 Color Palette

#### Primary Colors (Warm Terracotta)
| Token | Value | Usage |
|-------|-------|-------|
| `tandem-primary` | `#D97757` | Primary actions, self avatar, send button |
| `tandem-on-primary` | `#FFFFFF` | Text on primary |
| `tandem-primary-container` | `#FDF2EF` | Primary button backgrounds, active filter chip |
| `tandem-on-primary-container` | `#4A4238` | Text on primary container |

#### Secondary Colors
| Token | Value | Usage |
|-------|-------|-------|
| `tandem-secondary` | `#9C9488` | System avatar background |
| `tandem-secondary-container` | `#F0EBE6` | - |
| `tandem-on-secondary-container` | `#4A4238` | - |

#### Tertiary Colors (Partner/Coral)
| Token | Value | Usage |
|-------|-------|-------|
| `tandem-tertiary` | `#E07A5F` | Partner avatar, assignment card border, notification indicator |
| `tandem-tertiary-container` | `#FFE5DE` | Assignment card gradient top |

#### Background & Surface Colors
| Token | Value | Usage |
|-------|-------|-------|
| `background` | `#FFFBF7` | Screen background, nav bar, section headers |
| `on-background` | `#4A4238` | Primary text color |
| `surface` | `#FFFFFF` | Card background, input bar |
| `on-surface` | `#4A4238` | Card text |
| `surface-variant` | `#F0EBE6` | Secondary button background, input field |
| `on-surface-variant` | `#9C9488` | Secondary text, timestamps, action text |
| `outline` | `#E0DCD6` | Borders, dividers |
| `outline-light` | `#E0DCD6` | Completed checkbox fill |

#### Priority Colors
| Token | Value | Usage |
|-------|-------|-------|
| `priority-p1` | `#D1453B` | P1 checkbox border |
| `priority-p1-light` | `rgba(209, 69, 59, 0.1)` | P1 checkbox fill |
| `priority-p2` | `#EB8909` | P2 checkbox border |
| `priority-p2-light` | `rgba(235, 137, 9, 0.1)` | P2 checkbox fill |
| `priority-p3` | `#246FE0` | P3 checkbox border |
| `priority-p3-light` | `rgba(36, 111, 224, 0.1)` | P3 checkbox fill |
| `priority-p4` | `#79747E` | Default checkbox border |

#### Status Colors
| Token | Value | Usage |
|-------|-------|-------|
| `schedule-green` | `#058527` | Caught-up icon background |
| `unread-blue` | `#246FE0` | Unread indicator dot |

#### AI/System Colors
| Token | Value | Usage |
|-------|-------|-------|
| `ai-purple` | `#7C3AED` | AI avatar, AI card border, AI action button |
| `ai-purple-container` | `#F3E8FF` | AI card gradient top |

### 1.2 Typography

| Property | Value |
|----------|-------|
| Font Family | `-apple-system, BlinkMacSystemFont, 'SF Pro Text', 'Roboto', sans-serif` |
| Base Line Height | `1.5` |

| Element | Size | Weight | Color |
|---------|------|--------|-------|
| Nav Title | `28px` | `700` | `on-background` |
| Section Day | `14px` | `600` | `on-background` |
| Section Date | `14px` | `400` | `on-surface-variant` |
| Actor Name | `15px` | `600` | `on-surface` |
| Actor Action | `15px` | `400` | `on-surface-variant` |
| Card Timestamp | `12px` | `400` | `on-surface-variant` |
| Task Title | `14px` | `400` | `on-surface` |
| Task Meta | `11px` | `400` | `on-surface-variant` |
| Message Text | `14px` | `400` | `on-surface` |
| Filter Chip | `12px` | `500` | `on-surface-variant` (inactive) / `tandem-primary` (active) |
| Action Button | `13px` | `500` | varies |
| Caught-up Text | `12px` | `500` | `on-surface-variant` |
| Assignment Note | `13px` | `400` | `on-surface-variant` (italic) |

### 1.3 Spacing Scale

| Token | Value |
|-------|-------|
| `spacing-2` | `2px` |
| `spacing-4` | `4px` |
| `spacing-6` | `6px` |
| `spacing-8` | `8px` |
| `spacing-12` | `12px` |
| `spacing-16` | `16px` |
| `spacing-24` | `24px` |
| `spacing-32` | `32px` |

### 1.4 Border Radius

| Token | Value | Usage |
|-------|-------|-------|
| `radius-sm` | `8px` | Week event icon |
| `radius-md` | `12px` | Feed cards |
| `radius-lg` | `16px` | - |
| `radius-full` | `50px` | Avatars, buttons, chips, input field |

---

## 2. Screen Layout

### 2.1 Container
- **Max Width:** `390px`
- **Min Height:** `100vh`
- **Background:** `background` (#FFFBF7)
- **Box Shadow:** `0 0 40px rgba(0,0,0,0.1)` (for mockup frame effect)
- **Horizontal Margin:** `auto` (centered)

### 2.2 Vertical Structure (top to bottom)
1. Top Navigation Bar (sticky)
2. Filter Bar (below nav)
3. Feed Content (scrollable)
4. Message Input Bar (fixed at bottom)

### 2.3 Content Padding
- **Feed Content Bottom Padding:** `80px` (to clear input bar)

---

## 3. Top Navigation Bar

### 3.1 Container
- **Position:** `sticky`, `top: 0`
- **Z-Index:** `100`
- **Background:** `background` (#FFFBF7)
- **Padding:** `24px 16px 16px 16px` (top 24px, others 16px)
- **Border Bottom:** `1px solid outline` (#E0DCD6)

### 3.2 Layout
- **Display:** `flex`
- **Align Items:** `center`
- **Justify Content:** `space-between`

### 3.3 Title
- **Text:** "Feed"
- **Font Size:** `28px`
- **Font Weight:** `700`
- **Color:** `on-background` (#4A4238)
- **Letter Spacing:** `-0.5px`

### 3.4 Nav Button (More Options)
- **Size:** `40px × 40px`
- **Border Radius:** `radius-full` (50px)
- **Background:** `surface` (#FFFFFF)
- **Border:** none
- **Icon Size:** `20px × 20px`
- **Icon Color:** `on-surface` (#4A4238)
- **Hover Background:** `surface-variant` (#F0EBE6)

### 3.5 Nav Button Icon (Vertical Dots)
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <circle cx="12" cy="12" r="1"/>
  <circle cx="12" cy="5" r="1"/>
  <circle cx="12" cy="19" r="1"/>
</svg>
```

---

## 4. Filter Bar

### 4.1 Container
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-8` (8px)
- **Padding:** `8px 16px`
- **Background:** `background` (#FFFBF7)
- **Border Bottom:** `1px solid outline` (#E0DCD6)

### 4.2 Filter Chip (Default State)
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-4` (4px)
- **Padding:** `6px 12px`
- **Border Radius:** `radius-full` (50px)
- **Background:** `surface` (#FFFFFF)
- **Border:** `1px solid outline` (#E0DCD6)
- **Font Size:** `12px`
- **Font Weight:** `500`
- **Color:** `on-surface-variant` (#9C9488)
- **Transition:** `all 0.2s`

### 4.3 Filter Chip (Active State)
- **Background:** `tandem-primary-container` (#FDF2EF)
- **Border Color:** `tandem-primary` (#D97757)
- **Color:** `tandem-primary` (#D97757)

### 4.4 Filter Chip Icon (Checkmark - "All" chip only)
- **Size:** `14px × 14px`
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <polyline points="20 6 9 17 4 12"/>
</svg>
```

### 4.5 Filter Options
| Label | Has Icon |
|-------|----------|
| All | Yes (checkmark) |
| Tasks | No |
| Messages | No |

---

## 5. Section Headers (Sticky Day Headers)

### 5.1 Container
- **Position:** `sticky`
- **Top:** `89px` (below nav + filter bar)
- **Z-Index:** `50`
- **Background:** `background` (#FFFBF7)
- **Padding:** `12px 16px`
- **Border Bottom:** `1px solid outline` (#E0DCD6)

### 5.2 Content Layout
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-8` (8px)

### 5.3 Day Label
- **Font Size:** `14px`
- **Font Weight:** `600`
- **Color:** `on-background` (#4A4238)

### 5.4 Date Label
- **Font Size:** `14px`
- **Font Weight:** `400`
- **Color:** `on-surface-variant` (#9C9488)

### 5.5 Sample Headers
| Day | Date |
|-----|------|
| Today | Thursday, Jan 23 |
| Yesterday | Wednesday, Jan 22 |
| Monday | Jan 20 |

---

## 6. Feed Cards

### 6.1 Base Card Container
- **Margin:** `8px 16px`
- **Background:** `surface` (#FFFFFF)
- **Border Radius:** `radius-md` (12px)
- **Border:** `1px solid outline` (#E0DCD6)
- **Overflow:** `hidden`
- **Hover Shadow:** `0 2px 8px rgba(0,0,0,0.06)`
- **Animation:** `slideUp 0.3s ease forwards`

### 6.2 Card Header
- **Display:** `flex`
- **Align Items:** `flex-start`
- **Padding:** `12px 16px`
- **Gap:** `spacing-12` (12px)

### 6.3 Card Body
- **Border Top:** `1px solid outline` (#E0DCD6)
- **Padding:** `8px 16px`

### 6.4 Card Footer
- **Display:** `flex`
- **Justify Content:** `center`
- **Gap:** `spacing-8` (8px)
- **Padding:** `8px 16px`
- **Border Top:** `1px solid outline` (#E0DCD6)

### 6.5 Avatar Component
- **Size:** `36px × 36px`
- **Border Radius:** `50%`
- **Font Size:** `14px`
- **Font Weight:** `600`
- **Color:** `white`

| Type | Background Color | Content |
|------|------------------|---------|
| Partner | `tandem-tertiary` (#E07A5F) | First letter (e.g., "S") |
| Self | `tandem-primary` (#D97757) | First letter (e.g., "Y") |
| AI | `ai-purple` (#7C3AED) | Layers icon (18×18) |
| System | `tandem-secondary` (#9C9488) | User-plus icon (18×18) |

### 6.6 AI Avatar Icon (Layers)
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M12 2L2 7l10 5 10-5-10-5z"/>
  <path d="M2 17l10 5 10-5"/>
  <path d="M2 12l10 5 10-5"/>
</svg>
```

### 6.7 System Avatar Icon (User Plus)
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
  <circle cx="8.5" cy="7" r="4"/>
  <line x1="20" y1="8" x2="20" y2="14"/>
  <line x1="23" y1="11" x2="17" y2="11"/>
</svg>
```

### 6.8 Card Info Section
- **Flex:** `1`
- **Min Width:** `0` (enables text truncation)

### 6.9 Card Label Row
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-8` (8px)

### 6.10 Actor Name
- **Font Size:** `15px`
- **Font Weight:** `600`
- **Color:** `on-surface` (#4A4238)

### 6.11 Actor Action
- **Font Size:** `15px`
- **Font Weight:** `400`
- **Color:** `on-surface-variant` (#9C9488)

### 6.12 Timestamp
- **Font Size:** `12px`
- **Color:** `on-surface-variant` (#9C9488)
- **Margin Top:** `2px`

### 6.13 Unread Indicator (iOS-style dot)
Cards with class `unread` display a blue dot:
- **Position:** `absolute`
- **Top:** `8px`
- **Right:** `8px`
- **Size:** `10px × 10px`
- **Background:** `unread-blue` (#246FE0)
- **Border Radius:** `50%`
- **Z-Index:** `10`

---

## 7. Card Variants

### 7.1 Standard Message Card
- Uses base card styles
- **Header:** Avatar + Name + Timestamp
- **Body:** Message text only

**Message Text Style:**
- **Font Size:** `14px`
- **Color:** `on-surface` (#4A4238)
- **Line Height:** `1.5`

### 7.2 Task Completion Card
- Uses base card styles
- **Header:** Avatar + Name + "completed a task" + Timestamp
- **Body:** Completed task with checkbox

**Task Item Container:**
- **Display:** `flex`
- **Align Items:** `flex-start`
- **Gap:** `spacing-12` (12px)
- **Padding:** `0` (when inside card body)

**Task Checkbox (Completed State):**
- **Size:** `20px × 20px`
- **Border Radius:** `50%`
- **Border:** `2px solid outline-light` (#E0DCD6)
- **Background:** `outline-light` (#E0DCD6)
- **Margin Top:** `2px`

**Completed Checkbox Icon:**
- **Size:** `12px × 12px`
- **Color:** `white`
- **Stroke Width:** `3`
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
  <polyline points="20 6 9 17 4 12"/>
</svg>
```

**Completed Task Title:**
- **Text Decoration:** `line-through`
- **Color:** `on-surface-variant` (#9C9488)
- **Opacity:** `0.6`

**Notification Meta (when partner notified you):**
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-4` (4px)
- **Font Size:** `11px`
- **Color:** `tandem-tertiary` (#E07A5F)

**Notification Bell Icon:**
- **Size:** `12px × 12px`
- **Fill:** `currentColor`
```svg
<svg viewBox="0 0 24 24" fill="currentColor">
  <path d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.63-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.64 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z"/>
</svg>
```

### 7.3 Task Assignment Card
Special visual treatment for pending assignments.

**Card Container Overrides:**
- **Border:** `2px solid tandem-tertiary` (#E07A5F)
- **Background:** `linear-gradient(to bottom, tandem-tertiary-container, surface)` (#FFE5DE → #FFFFFF)

**Structure:**
1. Header: Partner avatar + "[Name] assigned you a task" + Timestamp
2. Body: Task with priority checkbox (uncompleted)
3. Assignment Note (optional): Quoted message from partner
4. Footer: Accept + Decline buttons

**Task Checkbox (Priority States):**

| Priority | Border Color | Background |
|----------|--------------|------------|
| P1 | `priority-p1` (#D1453B) | `priority-p1-light` (10% opacity) |
| P2 | `priority-p2` (#EB8909) | `priority-p2-light` (10% opacity) |
| P3 | `priority-p3` (#246FE0) | `priority-p3-light` (10% opacity) |
| P4/Default | `priority-p4` (#79747E) | `transparent` |

**Assignment Note:**
- **Font Size:** `13px`
- **Color:** `on-surface-variant` (#9C9488)
- **Font Style:** `italic`
- **Padding:** `0 16px 8px 16px`

**Footer Buttons:**

*Accept Button (Primary):*
- **Background:** `tandem-primary-container` (#FDF2EF)
- **Color:** `tandem-primary` (#D97757)
- **Padding:** `8px 16px`
- **Border Radius:** `radius-full` (50px)
- **Font Size:** `13px`
- **Font Weight:** `500`
- **Icon:** Checkmark (16×16) after text
- **Hover Background:** `tandem-tertiary-container` (#FFE5DE)

*Decline Button (Secondary):*
- **Background:** `surface-variant` (#F0EBE6)
- **Color:** `on-surface-variant` (#9C9488)
- **Padding:** `8px 16px`
- **Border Radius:** `radius-full` (50px)
- **Font Size:** `13px`
- **Font Weight:** `500`
- **Hover Background:** `outline` (#E0DCD6)

### 7.4 AI Prompt Card
Special visual treatment for AI prompts.

**Card Container Overrides:**
- **Border:** `2px solid ai-purple` (#7C3AED)
- **Background:** `linear-gradient(to bottom, ai-purple-container, surface)` (#F3E8FF → #FFFFFF)

**Structure:**
1. Header: AI avatar (layers icon) + "Tandem" + Timestamp
2. Body: Title + supporting text
3. Footer: AI action button + Dismiss button

**Body Content:**

*Prompt Title:*
- **Font Size:** `14px`
- **Font Weight:** `600`
- **Color:** `on-surface` (#4A4238)
- **Margin Bottom:** `4px`

*Supporting Text:*
- **Font Size:** `11px`
- **Color:** `on-surface-variant` (#9C9488)

**Footer Buttons:**

*AI Action Button:*
- **Background:** `ai-purple` (#7C3AED)
- **Color:** `white`
- **Padding:** `8px 16px`
- **Border Radius:** `radius-full` (50px)
- **Font Size:** `13px`
- **Font Weight:** `500`
- **Icon:** Chevron right (16×16) after text
- **Hover:** `opacity: 0.9`

*Dismiss Button:*
- **Background:** `surface-variant` (#F0EBE6)
- **Color:** `on-surface-variant` (#9C9488)
- **Padding:** `8px 16px`
- **Border Radius:** `radius-full` (50px)
- **Icon Only:** X icon (16×16)
- **Hover Background:** `outline` (#E0DCD6)

**X Icon (Dismiss):**
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <line x1="18" y1="6" x2="6" y2="18"/>
  <line x1="6" y1="6" x2="18" y2="18"/>
</svg>
```

**Chevron Right Icon:**
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <polyline points="9 18 15 12 9 6"/>
</svg>
```

### 7.5 Week Event Card (Planned/Reviewed)
Used for "finished planning" or "finished review" events.

**Structure:**
1. Header: Avatar + "[Name] finished planning" + Timestamp
2. Body: Week event preview

**Week Event Preview:**
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-12` (12px)

**Week Event Icon Container:**
- **Size:** `32px × 32px`
- **Border Radius:** `radius-sm` (8px)
- **Background:** `tandem-primary-container` (#FDF2EF)
- **Color:** `tandem-primary` (#D97757)

**Calendar Icon:**
- **Size:** `18px × 18px`
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
  <line x1="16" y1="2" x2="16" y2="6"/>
  <line x1="8" y1="2" x2="8" y2="6"/>
  <line x1="3" y1="10" x2="21" y2="10"/>
</svg>
```

**Week Event Title:**
- **Font Size:** `14px`
- **Font Weight:** `500`
- **Color:** `on-surface` (#4A4238)

**Week Event Meta:**
- **Font Size:** `12px`
- **Color:** `on-surface-variant` (#9C9488)

### 7.6 Partner Joined Card
System event for when a partner joins.

**Structure:**
1. Header only (no body): System avatar (user-plus) + "[Name] joined as your partner" + Timestamp

---

## 8. Caught Up Separator

Displayed after the last unread card to indicate user has seen all new content.

### 8.1 Container
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-12` (12px)
- **Padding:** `16px`
- **Margin:** `8px 16px`

### 8.2 Lines
- **Flex:** `1`
- **Height:** `1px`
- **Background:** `outline` (#E0DCD6)

### 8.3 Content Container
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-6` (6px)

### 8.4 Checkmark Icon Container
- **Size:** `20px × 20px`
- **Border Radius:** `50%`
- **Background:** `schedule-green` (#058527)
- **Color:** `white`

### 8.5 Checkmark Icon
- **Size:** `12px × 12px`
- **Stroke Width:** `3`
```svg
<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
  <polyline points="20 6 9 17 4 12"/>
</svg>
```

### 8.6 Text
- **Content:** "You're all caught up"
- **Font Size:** `12px`
- **Font Weight:** `500`
- **Color:** `on-surface-variant` (#9C9488)
- **White Space:** `nowrap`

---

## 9. Message Input Bar

### 9.1 Container
- **Position:** `fixed`
- **Bottom:** `0`
- **Left:** `50%`
- **Transform:** `translateX(-50%)`
- **Width:** `100%`
- **Max Width:** `390px`
- **Background:** `surface` (#FFFFFF)
- **Border Top:** `1px solid outline` (#E0DCD6)
- **Padding:** `12px 16px`
- **Display:** `flex`
- **Align Items:** `center`
- **Gap:** `spacing-12` (12px)
- **Z-Index:** `200`

### 9.2 Text Input Field
- **Flex:** `1`
- **Padding:** `12px 16px`
- **Background:** `surface-variant` (#F0EBE6)
- **Border:** none
- **Border Radius:** `radius-full` (50px)
- **Font Size:** `14px`
- **Font Family:** inherit
- **Color:** `on-surface` (#4A4238)
- **Focus Background:** `outline` (#E0DCD6)
- **Placeholder Color:** `on-surface-variant` (#9C9488)
- **Placeholder Text:** "Message Sarah..." (dynamic based on partner name)

### 9.3 Send Button
- **Size:** `40px × 40px`
- **Border Radius:** `50%`
- **Background:** `tandem-primary` (#D97757)
- **Color:** `white`
- **Border:** none
- **Hover:** `opacity: 0.9`
- **Active:** `transform: scale(0.95)`

**Disabled State:**
- **Background:** `outline` (#E0DCD6)
- **Color:** `on-surface-variant` (#9C9488)
- **Cursor:** `not-allowed`

### 9.4 Send Icon
- **Size:** `20px × 20px`
```svg
<svg viewBox="0 0 24 24" fill="currentColor">
  <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
</svg>
```

---

## 10. Animations

### 10.1 Card Entry Animation
```css
@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.feed-card {
  animation: slideUp 0.3s ease forwards;
}
```

### 10.2 Staggered Animation Delays
| Card Position | Delay |
|---------------|-------|
| 1st | 0ms |
| 2nd | 50ms |
| 3rd | 100ms |
| 4th | 150ms |

### 10.3 Dismiss Animation (AI prompts)
```css
.feed-card.dismissing {
  opacity: 0;
  transform: translateX(-100%);
  transition: all 0.3s ease;
}
```
After animation completes (300ms), remove the element from DOM.

### 10.4 Interaction Transitions
- **Button Background:** `transition: background 0.2s`
- **Card Hover Shadow:** `transition: box-shadow 0.2s`
- **Filter Chip:** `transition: all 0.2s`
- **Input Field Background:** `transition: background 0.2s`
- **Send Button:** `transition: opacity 0.2s, transform 0.2s`

---

## 11. Sample Data

The mockup displays the following items in order:

### Today - Thursday, Jan 23

| Order | Type | Actor | Time | Read State | Details |
|-------|------|-------|------|------------|---------|
| 1 | AI Plan Prompt | Tandem | 8:00 AM | Unread | Title: "Ready to plan your week?", Meta: "You have 3 tasks rolled over from last week." |
| 2 | Task Assignment | Sarah | 9:30 AM | Unread | Task: "Pick up birthday cake from bakery" (P2), Note: "Don't forget it's the chocolate one! They close at 6pm" |
| 3 | Message | Sarah | 2:30 PM | Unread | "Running about 10 minutes late! Can you start dinner prep?" |
| — | Caught Up Separator | — | — | — | — |
| 4 | Task Completion | Sarah | 11:42 AM | Read | Task: "Grocery shopping", Has notification indicator |
| 5 | Task Completion | You | 10:15 AM | Read | Task: "Book flight tickets" |

### Yesterday - Wednesday, Jan 22

| Order | Type | Actor | Time | Read State | Details |
|-------|------|-------|------|------------|---------|
| 6 | Week Planned | Sarah | 8:00 PM | Read | "Week of Jan 20", "Planned 8 tasks" |
| 7 | Message | You | 3:42 PM | Read | "Sounds good! I'll handle dinner tonight." |
| 8 | Task Accepted | Sarah | 10:00 AM | Read | Task: "Call insurance company" (P3) |

### Monday - Jan 20

| Order | Type | Actor | Time | Read State | Details |
|-------|------|-------|------|------------|---------|
| 9 | Partner Joined | Sarah | 2:30 PM | Read | System event |
| 10 | Task Completion | You | 9:00 AM | Read | Task: "Set up weekly goals" |

---

## Appendix: Action Labels by Event Type

| Event Type | Actor Action Text |
|------------|-------------------|
| Task Completed | "completed a task" |
| Task Assigned | "assigned you a task" |
| Task Accepted | "accepted your task" |
| Task Declined | "declined your task" |
| Week Planned | "finished planning" |
| Week Reviewed | "finished review" |
| Partner Joined | "joined as your partner" |
| Message | (no action text, name only) |
| AI Prompt | (no action text, "Tandem" only) |
