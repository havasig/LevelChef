import SwiftUI
import UIKit
import LevelChefShared

/// Wraps `shared`'s Compose Multiplatform view controller (`IosEntryPointKt.MainViewController()`)
/// and forwards iOS system appearance (light/dark) changes into Kotlin (`IosThemeBridge`), since
/// Compose Multiplatform's own `isSystemInDarkTheme()` doesn't reliably track iOS appearance
/// changes (JetBrains/compose-multiplatform#3575) — see `IosThemeBridge.kt`'s doc comment.
private final class ThemedHostingController: UIViewController {
    private let child = IosEntryPointKt.MainViewController()

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(child)
        view.addSubview(child.view)
        child.view.frame = view.bounds
        child.view.autoresizingMask = [.flexibleWidth, .flexibleHeight]
        child.didMove(toParent: self)
        reportAppearance()
    }

    override func traitCollectionDidChange(_ previousTraitCollection: UITraitCollection?) {
        super.traitCollectionDidChange(previousTraitCollection)
        if traitCollection.userInterfaceStyle != previousTraitCollection?.userInterfaceStyle {
            reportAppearance()
        }
    }

    private func reportAppearance() {
        IosThemeBridge.shared.onSystemAppearanceChanged(isDark: traitCollection.userInterfaceStyle == .dark)
    }
}

/// Wraps `shared`'s full bottom-nav graph (`IosEntryPointKt.MainViewController()` boots
/// `SharedApp()` behind `OnboardingGate`) — see `IosEntryPoint.kt`.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        ThemedHostingController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.container, edges: .all)
    }
}
