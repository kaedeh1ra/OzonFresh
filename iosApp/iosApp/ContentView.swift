import Shared
import SwiftUI
import UIKit

/// Экран Compose из фреймворка Shared (app/shared, функция MainViewController в iosMain).
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // Отступы от выреза и home-индикатора делает Scaffold оболочки.
        ComposeView().ignoresSafeArea()
    }
}
