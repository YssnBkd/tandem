# Tandem — Product Vision

## The Problem

People struggle to maintain long-term goals and resolutions. Whether it's New Year's resolutions, birthday commitments, or quarterly objectives, the pattern is predictable:

1. **Initial enthusiasm** — Goals are set with good intentions
2. **Gradual drift** — Daily life takes over, focus fades
3. **Loss of continuity** — Progress becomes invisible, motivation disappears
4. **Abandonment** — Goals are forgotten until the next resolution moment

The fundamental issue isn't lack of willpower—it's lack of **visibility**, **accountability**, and **continuity**.

---

## The Solution

**Tandem** is a week-centered task management app designed for couples (or close accountability partners) that combines three powerful forces:

### 1. Partner Accountability

Your life partner becomes your accountability partner. This transforms goal pursuit from a solitary struggle into a team effort.

- **Shared visibility** — Partners see each other's tasks and progress
- **Mutual assignment** — Partners can assign tasks to each other
- **Completion notifications** — Get notified when your partner finishes a task
- **In-app messaging** — Discuss tasks, celebrate wins, offer support

The psychological shift is profound: you're no longer just letting yourself down when you skip a task—you're visible to someone who cares about your growth.

### 2. Weekly Rhythm

The week is the atomic unit of productivity. Long enough to accomplish meaningful work, short enough to course-correct.

- **Weekly planning** — Start each week by deciding what matters
- **Weekly review** — End each week by reflecting on what happened
- **Rolling tasks** — Unfinished tasks can roll forward intentionally
- **Historical timeline** — Scroll through past weeks to see your journey

This rhythm creates natural checkpoints that prevent goals from drifting into oblivion.

### 3. LLM-Powered Life Graph

This is where Tandem becomes transformative. An LLM assistant that:

- **Captures adaptively** — During conversations, it extracts and stores:
  - Tasks (things to do)
  - Entities (people, places, projects you mention)
  - Goals (what you're working toward)
  - Feelings (how you're doing emotionally)
  - Insights (patterns and realizations)

- **Builds context over time** — Each conversation enriches a "life graph" that represents your world: your priorities, relationships, recurring themes, and evolving goals

- **Provides informed guidance** — With accumulated context, the LLM can:
  - Suggest tasks based on your stated goals
  - Notice when you're overcommitting
  - Remind you of past patterns ("Last time you tried this, here's what happened")
  - Celebrate progress you might not notice yourself

Think of it like Todoist's "Ramble" feature, but instead of just transcribing tasks, it understands your life.

---

## Core User Journeys

### Weekly Planning

> "What do you want to focus on this week?"

The user opens Tandem on Sunday evening or Monday morning. The LLM greets them with context:

- "Last week you completed 8/12 tasks. You mentioned feeling overwhelmed by the home renovation project."
- "You have a recurring goal to exercise 3x/week—you hit it last week!"
- "Your partner assigned you 'Schedule dentist appointment' last week, but it rolled over."

Through conversation, the user plans their week. The LLM captures tasks, links them to goals, and suggests priorities based on history.

### Weekly Review

> "How did your week go?"

At the end of the week, the LLM guides reflection:

- "You completed the project proposal—how do you feel about it?"
- "You skipped your workout sessions this week. What got in the way?"
- "I noticed you added 5 tasks mid-week related to the car repair. Was that unexpected?"

The review isn't just about task completion—it's about understanding life patterns.

### Timeline Exploration

> "Let me see how far I've come."

The user scrolls through their timeline, seeing weeks organized by month, quarter, and year. Each week shows:

- Tasks completed vs. planned
- (Future) Summary from their review conversation
- (Future) Key insights or milestones

This creates a **life journal** that emerges naturally from weekly task management.

### Partner Collaboration

> "Can you handle the insurance claim this week?"

Partners assign tasks to each other, add context, and get notified on completion. The app facilitates:

- Task delegation with context ("I'll be traveling, can you handle this?")
- Shared goals ("Let's both exercise 3x this week")
- Celebration ("Sarah completed all her tasks this week!")
- Support ("You mentioned feeling stressed—anything I can help with?")

---

## Product Principles

### 1. Week as the Anchor

Everything revolves around the week. Not the day (too granular), not the month (too distant). The week provides the right balance of planning horizon and feedback loop.

### 2. Visibility Creates Accountability

Making progress visible—to yourself, to your partner, over time—is the core mechanism for sustained motivation. The timeline, partner sharing, and review summaries all serve this purpose.

### 3. Context is Everything

The LLM's value grows with context. Every conversation, every completed task, every reflection adds to its understanding. This compounds over time, making the assistant increasingly valuable.

### 4. Conversation Over Forms

Users shouldn't fill out forms to plan their week. They should have a conversation. The LLM extracts structure from natural dialogue, reducing friction while capturing richer information.

### 5. Judgment-Free Reflection

The app observes patterns without moralizing. "You skipped workouts 3 weeks in a row" is an observation, not a judgment. Users can reflect on what's happening without feeling shamed by their tools.

---

## Feature Roadmap

### Foundation (Current)
- [x] Week-based task management
- [x] Task creation, completion, scheduling
- [x] Priority levels (P1-P4)
- [x] Goals linked to tasks
- [x] Partner system (invites, pairing)
- [x] Timeline screen (weeks, months, quarters)

### Phase 1: LLM Integration
- [ ] Planning conversation flow
- [ ] Review conversation flow
- [ ] Basic task extraction from conversation
- [ ] Week summary generation

### Phase 2: Life Graph
- [ ] Entity extraction (people, projects, places)
- [ ] Goal tracking with progress inference
- [ ] Feeling/mood capture
- [ ] Insight detection and storage

### Phase 3: Intelligent Assistance
- [ ] Context-aware suggestions during planning
- [ ] Pattern recognition ("You tend to overcommit on Mondays")
- [ ] Proactive reminders based on history
- [ ] Cross-week insight synthesis

### Phase 4: Partner Intelligence
- [ ] Shared context between partners
- [ ] Relationship-aware suggestions
- [ ] Collaborative goal tracking
- [ ] Partner workload balancing suggestions

### Phase 5: Long-term Reflection
- [ ] Monthly/quarterly summaries
- [ ] Year-in-review generation
- [ ] Life chapter detection ("This was your fitness transformation quarter")
- [ ] Exportable life journal

---

## Success Metrics

### Engagement
- Weekly active users (WAU)
- Planning session completion rate
- Review session completion rate
- Partner message frequency

### Retention
- Week-over-week retention
- 8-week retention (habit formation threshold)
- Partner pair retention vs. solo users

### Effectiveness
- Task completion rate over time
- Goal achievement rate
- User-reported satisfaction with progress
- "Visibility" score (how often users check timeline)

### LLM Value
- Conversation length (proxy for engagement)
- Suggestion acceptance rate
- User corrections to extracted data
- Context recall accuracy (can LLM reference past conversations appropriately?)

---

## Competitive Positioning

| App | Strength | Tandem Differentiator |
|-----|----------|----------------------|
| Todoist | Powerful task management | Partner accountability + LLM context |
| Notion | Flexible workspace | Week-centered simplicity + conversation UX |
| Day One | Journaling | Journaling emerges from task management |
| Paired | Couple activities | Productivity-focused, not relationship games |
| ChatGPT | Conversational AI | Persistent context + integrated task execution |

Tandem sits at the intersection of **productivity apps** and **personal AI assistants**, specifically designed for **couples** with a focus on **long-term continuity**.

---

## The Vision

Imagine opening Tandem a year from now:

> "Hey, you've been using Tandem for 52 weeks now. Here's what I've noticed:
>
> You started the year wanting to get healthier, learn Spanish, and finish the basement renovation. The renovation took longer than expected—it dominated Q1—but you finished it in March.
>
> Your Spanish practice was inconsistent until you and Sarah started doing it together in Q3. You've now maintained a 12-week streak.
>
> Health-wise, you've exercised an average of 2.1 times per week, up from 0.8 at the start. Your highest streak was 8 weeks in the summer.
>
> You've completed 847 tasks this year. The ones you struggled with most were administrative tasks—maybe we should talk about that pattern.
>
> What would you like to focus on in the coming year?"

This is Tandem: an AI that knows your life, helps you stay on track, and celebrates how far you've come.

---

## Appendix: Life Graph Schema (Conceptual)

```
User
├── Weeks[]
│   ├── Tasks[]
│   ├── ReviewConversation
│   └── PlanningConversation
├── Goals[]
│   ├── LinkedTasks[]
│   └── ProgressSnapshots[]
├── Entities[]
│   ├── People (Sarah, Mom, Boss)
│   ├── Projects (Basement Renovation, Wedding Planning)
│   └── Places (Gym, Office, Home)
├── Feelings[]
│   └── (timestamp, sentiment, context)
├── Insights[]
│   └── (pattern, evidence, timestamp)
└── Partner
    └── SharedContext
```

The life graph is not a rigid database—it's a semantic network that the LLM builds and queries to provide contextual assistance.
