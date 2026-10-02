import ActivityKit
import ComposeApp
import Foundation

/// Reaches ActivityKit for the shared code, which has no way to it from Kotlin. What to show and
/// when is decided there; this only passes it on.
final class LiveActivityController: NSObject, PhaseLiveActivity {
    var isShowing: Bool {
        Activity<PhaseAttributes>.activities.contains {
            $0.activityState == .active || $0.activityState == .stale
        }
    }

    func start(current: LiveActivityPhase, next: LiveActivityPhase) {
        let state = PhaseAttributes.ContentState(current: .init(current), next: .init(next))
        Task {
            for activity in Activity<PhaseAttributes>.activities {
                await activity.end(nil, dismissalPolicy: .immediate)
            }
            // Stale from the end of the phase, which is when the widget turns to the next one.
            let content = ActivityContent(state: state, staleDate: state.current.endsAt)
            _ = try? Activity.request(attributes: PhaseAttributes(), content: content)
        }
    }
}

private extension PhaseAttributes.Phase {
    init(_ phase: LiveActivityPhase) {
        self.init(
            isFasting: phase.isFasting,
            title: phase.title,
            caption: phase.caption,
            endsAt: Date(timeIntervalSince1970: phase.endsAtEpochSeconds)
        )
    }
}
