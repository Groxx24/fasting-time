import ActivityKit
import SwiftUI
import WidgetKit

@main
struct PhaseActivityBundle: WidgetBundle {
    var body: some Widget {
        PhaseActivityWidget()
    }
}

/// The phase and its countdown on the Lock Screen and in the Dynamic Island. This is the one piece
/// of UI that cannot be drawn in Compose, because the system renders it outside the app.
struct PhaseActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: PhaseAttributes.self) { context in
            let phase = context.phase
            VStack(spacing: 2) {
                Text(phase.title.uppercased())
                    .font(.subheadline.weight(.medium))
                    .kerning(3)
                Countdown(phase: phase)
                    .font(.system(size: 44, weight: .black))
                    .multilineTextAlignment(.center)
                Text(phase.caption)
                    .font(.subheadline)
            }
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .activityBackgroundTint(phase.sky)
            .activitySystemActionForegroundColor(.white)
        } dynamicIsland: { context in
            let phase = context.phase
            return DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Image(systemName: phase.symbol)
                }
                DynamicIslandExpandedRegion(.center) {
                    Text(phase.title)
                        .font(.headline)
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(spacing: 2) {
                        Countdown(phase: phase)
                            .font(.system(size: 36, weight: .black))
                            .multilineTextAlignment(.center)
                        Text(phase.caption)
                            .font(.subheadline)
                    }
                }
            } compactLeading: {
                Image(systemName: phase.symbol)
            } compactTrailing: {
                // A timer takes all the width it is offered, so offer what "00:00:00" needs.
                Countdown(phase: phase)
                    .multilineTextAlignment(.trailing)
                    .frame(width: 66)
            } minimal: {
                Image(systemName: phase.symbol)
            }
        }
    }
}

/// Counts down by itself, without the app running or sending anything.
private struct Countdown: View {
    let phase: PhaseAttributes.Phase

    var body: some View {
        // The range must not run backwards, which it would once the phase is over.
        Text(timerInterval: Date.now...max(Date.now, phase.endsAt), countsDown: true)
            .monospacedDigit()
    }
}

private extension ActivityViewContext<PhaseAttributes> {
    /// The activity goes stale at the end of the phase it was started in.
    var phase: PhaseAttributes.Phase {
        isStale ? state.next : state.current
    }
}

private extension PhaseAttributes.Phase {
    var symbol: String {
        isFasting ? "moon.stars.fill" : "fork.knife"
    }

    /// The middle colour of the backdrop's sky for this phase.
    var sky: Color {
        isFasting
            ? Color(red: 0x1E / 255, green: 0x1B / 255, blue: 0x4B / 255)
            : Color(red: 0xDB / 255, green: 0x27 / 255, blue: 0x77 / 255)
    }
}
