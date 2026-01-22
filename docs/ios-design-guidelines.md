# iOS Design Guidelines Reference

Official Apple Human Interface Guidelines - quantified values for layout, typography, and spacing.

## Typography - Text Styles

Default values at Large (default) content size category:

| Text Style | Size | Weight | Line Height | Letter Spacing |
|------------|------|--------|-------------|----------------|
| Large Title | 34pt | Regular | 41pt | +0.37px |
| Extra Large Title (iOS 17+) | 36pt | Bold | — | — |
| Extra Large Title 2 (iOS 17+) | 28pt | Bold | — | — |
| Title 1 | 28pt | Regular | 34pt | +0.36px |
| Title 2 | 22pt | Regular | 28pt | +0.35px |
| Title 3 | 20pt | Regular | — | — |
| Headline | 17pt | Semibold | 22pt | — |
| Body | 17pt | Regular | 22pt | −0.41px |
| Callout | 16pt | Regular | — | — |
| Subheadline | 15pt | Regular | — | −0.24px |
| Footnote | 13pt | Regular | — | −0.08px |
| Caption 1 | 12pt | Regular | 16pt | 0px |
| Caption 2 | 11pt | Regular | 13pt | +0.07px |

### Font Variants

- **SF Pro Display**: Use for font sizes ≥ 20pt
- **SF Pro Text**: Use for font sizes < 20pt
- iOS automatically selects the correct variant when using system fonts

### Typography Best Practices

1. Use **weight and color** for hierarchy rather than many size variations
2. Primary text: 17pt (Body)
3. Secondary text: 15pt (Subheadline) with lighter color
4. Tertiary text: 13pt (Footnote)
5. Minimum readable size: 11pt (Caption 2)
6. Tab bar labels: 10pt (smallest in iOS)

## Touch Targets

| Element | Minimum Size |
|---------|-------------|
| All interactive elements | **44 × 44 pt** |
| Buttons | 44pt minimum height |
| Tab bar icons | 25–31pt |
| Icon buttons | 44pt touch area (icon can be smaller) |

**Critical**: Elements smaller than 44×44pt are missed by >25% of users.

## Spacing System (8-Point Grid)

iOS follows an **8-point grid system**. All spacing should be multiples of 8:

| Token | Value | Usage |
|-------|-------|-------|
| xs | 4pt | Icon gaps, tight spacing |
| sm | 8pt | Between related items |
| md | 16pt | Standard margins, padding |
| lg | 24pt | Section spacing, card gaps |
| xl | 32pt | Major section breaks |
| xxl | 40pt | Page-level spacing |

### Standard Margins

| Context | Value |
|---------|-------|
| iPhone horizontal margins | **16pt** |
| iPad horizontal margins | **24pt** |
| List item internal padding | 12–16pt |
| Space between list items | 8pt |
| Card spacing (simple) | 16pt |
| Card spacing (complex) | 24pt |
| Input field padding | 12–16pt |

## Component Heights

| Component | iPhone | iPad |
|-----------|--------|------|
| Status bar | 54pt (notch), 20pt (legacy) | 24pt |
| Navigation bar | 44pt | 44pt |
| Navigation bar + large title | 96pt | 96pt |
| Tab bar | 49pt | 50pt |
| Toolbar | 44pt | 50pt |
| Home indicator | 34pt | — |
| Search bar | 36pt | 36pt |

### Safe Areas

- Top: Status bar height + navigation bar
- Bottom: Home indicator (34pt on notched devices)
- Always respect safe area insets

## Dynamic Type Scaling

### Content Size Categories

Standard (7 levels): xSmall, Small, Medium, **Large (Default)**, xLarge, xxLarge, xxxLarge

Accessibility (5 additional): AX1, AX2, AX3, AX4, AX5

### Body Text Scaling Table

| Category | Size |
|----------|------|
| xSmall | 14pt |
| Small | 15pt |
| Medium | 16pt |
| **Large (Default)** | **17pt** |
| xLarge | 19pt |
| xxLarge | 21pt |
| xxxLarge | 23pt |
| AX1 | 28pt |
| AX2 | 33pt |
| AX3 | 40pt |
| AX4 | 47pt |
| AX5 | 53pt |

### Scaling Behavior

- Larger default sizes have smaller scale factors
- Caption 2 has minimum size of 11pt (won't go smaller)
- Support Dynamic Type for accessibility compliance

## Contrast & Accessibility

| Requirement | Minimum Ratio |
|-------------|---------------|
| Standard text (< 18pt) | 4.5:1 |
| Large text (≥ 18pt bold or ≥ 24pt) | 3:1 |
| UI components | 3:1 |

## Layout Patterns

### Lists

- Row height: minimum 44pt for tappable rows
- Separator inset: typically 16pt from leading edge
- Accessory spacing: 8pt from trailing edge

### Cards

- Corner radius: 10–16pt (system default ~10pt)
- Internal padding: 16pt
- Shadow: subtle, ~2pt blur

### Navigation

- Large title visible: Content starts below 96pt navigation area
- Collapsed title: Content starts below 44pt navigation bar
- Scroll threshold: Large title collapses after ~20pt of scroll

## Implementation Checklist

### Typography
- [ ] Body text is 17pt
- [ ] Secondary text is 15pt with reduced opacity/lighter color
- [ ] Titles use appropriate text styles (not arbitrary sizes)
- [ ] Letter spacing matches Apple specifications
- [ ] Dynamic Type is supported

### Spacing
- [ ] Horizontal margins are 16pt (iPhone)
- [ ] All spacing uses 8pt grid multiples
- [ ] Consistent spacing tokens throughout

### Touch Targets
- [ ] All interactive elements are ≥ 44×44pt
- [ ] Buttons have adequate padding
- [ ] List rows are tappable across full width

### Accessibility
- [ ] Contrast ratios meet 4.5:1 minimum
- [ ] Dynamic Type scales text appropriately
- [ ] Touch targets remain accessible at larger text sizes

## Sources

- [Apple HIG - Typography](https://developer.apple.com/design/human-interface-guidelines/typography)
- [Apple HIG - Layout](https://developer.apple.com/design/human-interface-guidelines/layout)
- [iOS Font Sizes Reference](https://www.iosfontsizes.com/)
- [iOS Design Guidelines](https://ivomynttinen.com/blog/ios-design-guidelines/)
- [Learn UI Design - iOS Font Size Guidelines](https://www.learnui.design/blog/ios-font-size-guidelines.html)
