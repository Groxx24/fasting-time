import ActivityKit
import Foundation

/// What the Live Activity shows. The words come from the shared Kotlin code, so the widget only
/// lays them out. Compiled into both the app, which starts the activity, and the widget.
struct PhaseAttributes: ActivityAttributes {
    struct Phase: Codable, Hashable {
        var isFasting: Bool
        var title: String
        var caption: String
        var endsAt: Date
    }

    struct ContentState: Codable, Hashable {
        /// The phase under way when the activity was started.
        var current: Phase
        /// The phase after it, which the widget turns to by itself once `current` is over.
        var next: Phase
    }
}
