import SwiftUI
import UIKit

@objc(ForgeNativeDiagnostics)
public final class ForgeNativeDiagnostics: NSObject {
    @objc(presentWithVersion:phase:cardCount:platform:details:)
    public static func present(
        version: String,
        phase: String,
        cardCount: String,
        platform: String,
        details: String
    ) {
        DispatchQueue.main.async {
            guard let presenter = ForgeNativePresenter.topViewController() else { return }

            let view = ForgeDiagnosticsView(
                version: version,
                phase: phase,
                cardCount: cardCount,
                platform: platform,
                details: details
            )
            let controller = UIHostingController(rootView: view)
            controller.modalPresentationStyle = .formSheet
            presenter.present(controller, animated: true)
        }
    }
}

@objc(ForgeNativeDeckLibrary)
public final class ForgeNativeDeckLibrary: NSObject {
    @objc(presentWithDecksJSON:)
    public static func present(decksJSON: String) {
        let data = Data(decksJSON.utf8)
        let decks = (try? JSONDecoder().decode([NativeDeckSummary].self, from: data)) ?? []

        DispatchQueue.main.async {
            guard let presenter = ForgeNativePresenter.topViewController() else { return }
            let controller = UIHostingController(rootView: ForgeDeckLibraryView(decks: decks))
            controller.modalPresentationStyle = .formSheet
            presenter.present(controller, animated: true)
        }
    }
}

private enum ForgeNativePresenter {
    static func topViewController(
        from base: UIViewController? = keyWindow()?.rootViewController
    ) -> UIViewController? {
        if let navigation = base as? UINavigationController {
            return topViewController(from: navigation.visibleViewController)
        }
        if let tabs = base as? UITabBarController {
            return topViewController(from: tabs.selectedViewController)
        }
        if let presented = base?.presentedViewController {
            return topViewController(from: presented)
        }
        return base
    }

    private static func keyWindow() -> UIWindow? {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }
    }
}

private struct NativeDeckSummary: Decodable, Identifiable {
    let id: String
    let name: String
    let category: String
    let path: String
    let mainCount: Int
    let sideboardCount: Int
    let commanderCount: Int
}

private struct ForgeDeckLibraryView: View {
    @Environment(\.presentationMode) private var presentationMode

    let decks: [NativeDeckSummary]

    private var categories: [String] {
        decks.reduce(into: []) { result, deck in
            if !result.contains(deck.category) {
                result.append(deck.category)
            }
        }
    }

    private var deckCountLabel: String {
        decks.count == 1 ? "1 saved deck" : "\(decks.count) saved decks"
    }

    var body: some View {
        NavigationView {
            List {
                if decks.isEmpty {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("No saved decks")
                                .font(.headline)
                            Text("Create or import a deck in Forge, then reopen this native preview.")
                                .foregroundColor(.secondary)
                        }
                        .padding(.vertical, 12)
                    }
                } else {
                    ForEach(categories, id: \.self) { category in
                        Section(header: Text(category)) {
                            ForEach(decks.filter { $0.category == category }) { deck in
                                NativeDeckRow(deck: deck)
                            }
                        }
                    }
                }

                Section(footer: Text("Read-only preview · \(deckCountLabel)")) {
                    EmptyView()
                }
            }
            .listStyle(InsetGroupedListStyle())
            .navigationTitle("Deck Library")
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

private struct NativeDeckRow: View {
    let deck: NativeDeckSummary

    private var counts: String {
        var values = ["\(deck.mainCount) main"]
        if deck.commanderCount > 0 {
            values.append("\(deck.commanderCount) commander")
        }
        if deck.sideboardCount > 0 {
            values.append("\(deck.sideboardCount) sideboard")
        }
        return values.joined(separator: " · ")
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(deck.name)
                .font(.headline)
            Text(counts)
                .font(.subheadline)
                .foregroundColor(.secondary)
            if !deck.path.isEmpty {
                Text(deck.path)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
        }
        .padding(.vertical, 3)
    }
}

private struct ForgeDiagnosticsView: View {
    @Environment(\.presentationMode) private var presentationMode

    let version: String
    let phase: String
    let cardCount: String
    let platform: String
    let details: String

    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Engine")) {
                    DiagnosticRow(label: "Status", value: phase.replacingOccurrences(of: "_", with: " "))
                    DiagnosticRow(label: "Version", value: version)
                    DiagnosticRow(label: "Card printings", value: cardCount)
                    DiagnosticRow(label: "Platform", value: platform)
                    DiagnosticRow(label: "Runtime", value: "MobiVM AOT")
                }

                Section(header: Text("Device")) {
                    Text(details)
                        .font(.system(.footnote, design: .monospaced))
                }

                Section(footer: Text("Forge is free software licensed under GPL-3.0.")) {
                    Link("View source", destination: URL(string: "https://github.com/yikaret/forge")!)
                }
            }
            .listStyle(InsetGroupedListStyle())
            .navigationTitle("Forge Diagnostics")
            .navigationBarItems(trailing: Button("Done") {
                presentationMode.wrappedValue.dismiss()
            })
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
}

private struct DiagnosticRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(label)
            Spacer()
            Text(value)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.trailing)
        }
    }
}
