import SwiftUI
import LevelChefShared

@main
struct IOSApp: App {

    init() {
        // Mirrors `LevelChefApplication.onCreate()`'s `startKoin` call on Android — see
        // `shared/src/iosMain/kotlin/com/levelchef/shared/IosEntryPoint.kt`.
        IosEntryPointKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
