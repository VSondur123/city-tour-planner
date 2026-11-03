# Self-evaluation

## 1. How well have we been able to stick to our original design?

Since we started discussing this project we have stayed with one approach, because the team has been communicating throughout. Our original design is still intact.

The one change we had to make was forced by the lack of an open API that provides tour information for all locations. We moved to storing place information in our own JSON file, and we asked our TA for approval of this change.

> [Team to confirm before submission: list any other structural changes to the prototype design here, so that all changes are documented.]

## 2. How well have we been able to implement features based on our original plan?

We have been able to implement features fairly well according to the original plan. The plan has not changed so far, so nothing has needed to be dropped or reworked.

> [Update before submission with what is actually implemented at the tagged commit.]

## 3. How does our design correspond to quality?

Given what we have been presented with in the course, we believe we are using the right approaches and the correct patterns: Adapter, Facade and Proxy (with Strategy for stop selection).

We think this lets us reach better quality than building the application from simple classes. This matters especially because external APIs can fail, and we have already seen this happen. Our design handles a failing provider explicitly instead of letting the whole request fail.

## 4. What changes do we anticipate to implement the remaining features?

We expect to make a few changes here and there inside the Strategy part of the design, where the stop-selection logic is still being worked out.

## 5. Individual tasks accomplished or changed per group member

### Varun Ganesh Sondur
- Designed the overall repository structure.
- Identified the proper design patterns for the application.
- Built the API Research Runner, and identified that we needed to move to a repository structure.
- Designed the high-level components and interfaces, and carried out the analysis behind them.

### Thanh
- [To be filled in by Thanh.]

### Georgios Malezoglou
- Working on research and development for the maps.
- Working with Varun on the progress of the API Research Runner.
- [Add anything else, to be filled in by Georgios.]

### Abdullah
- [To be filled in by Abdullah.]

> [Where any member's contribution differs from the prototype submission, explain the difference here.]
