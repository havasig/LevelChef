import SwiftUI
import UIKit
import LevelChefShared

/// Wraps `shared`'s Compose Multiplatform Home screen — see `IosEntryPointKt.MainViewController()`.
/// Minimal shell: just Home, no navigation (see AGENTS.md's iOS "Not yet done" item).
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        IosEntryPointKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(.container, edges: .all)
    }
}
