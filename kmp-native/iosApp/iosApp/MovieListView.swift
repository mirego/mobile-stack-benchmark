import SwiftUI
import shared

@MainActor
class MovieListObservable: ObservableObject {
    @Published var uiState: MovieListUiState

    private let viewModel: MovieListViewModel
    private var observer: MovieListStateObserver?

    init() {
        let helper = KoinHelper()
        self.viewModel = helper.getMovieListViewModel()
        self.uiState = viewModel.uiState.value as! MovieListUiState
        observeState()
    }

    private func observeState() {
        observer = MovieListStateObserver(viewModel: viewModel) { [weak self] state in
            DispatchQueue.main.async {
                self?.uiState = state
            }
        }
    }

    func loadNextPage() {
        viewModel.loadNextPage()
    }

    func retry() {
        viewModel.retry()
    }

    deinit {
        observer?.close()
    }
}

struct MovieListView: View {
    @StateObject private var observable = MovieListObservable()

    var body: some View {
        NavigationStack {
            Group {
                if observable.uiState.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = observable.uiState.error, observable.uiState.movies.isEmpty {
                    VStack(spacing: 16) {
                        Text(error)
                            .foregroundColor(.red)
                            .multilineTextAlignment(.center)
                        Button("Retry") {
                            observable.retry()
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    List {
                        ForEach(observable.uiState.movies, id: \.id) { movie in
                            NavigationLink(destination: MovieDetailView(movieId: movie.id)) {
                                MovieListItemView(movie: movie)
                            }
                            .onAppear {
                                if movie.id == observable.uiState.movies.last?.id {
                                    observable.loadNextPage()
                                }
                            }
                        }
                        if observable.uiState.isLoadingMore {
                            HStack {
                                Spacer()
                                ProgressView()
                                Spacer()
                            }
                            .listRowSeparator(.hidden)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("KMP-Native")
        }
    }
}

struct MovieListItemView: View {
    let movie: Movie

    private var filledStars: Int {
        min(5, max(0, Int((movie.voteAverage / 2).rounded())))
    }

    private var starText: String {
        String(repeating: "★", count: filledStars) + String(repeating: "☆", count: 5 - filledStars)
    }

    private var pillColor: Color {
        if movie.voteAverage >= 7 { return Color(red: 0.298, green: 0.686, blue: 0.314) }
        if movie.voteAverage >= 5 { return Color(red: 1.0, green: 0.596, blue: 0.0) }
        return Color(red: 0.957, green: 0.263, blue: 0.212)
    }

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            if let posterPath = movie.posterPath {
                AsyncImage(url: URL(string: "https://image.tmdb.org/t/p/w342\(posterPath)")) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    case .failure:
                        posterPlaceholder
                    case .empty:
                        ProgressView()
                    @unknown default:
                        posterPlaceholder
                    }
                }
                .frame(width: 80, height: 120)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                posterPlaceholder
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(movie.title)
                    .font(.headline)
                    .lineLimit(2)
                Text(String(movie.releaseDate.prefix(4)))
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                HStack(spacing: 4) {
                    Text(starText)
                        .font(.subheadline)
                        .foregroundColor(.orange)
                    Text(String(format: "%.1f", movie.voteAverage))
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                Text("\(NumberFormatter.localizedString(from: NSNumber(value: movie.voteCount), number: .decimal)) votes")
                    .font(.caption)
                    .foregroundColor(.secondary)
                Text(movie.overview)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(3)
                Text(String(format: "%.1f", movie.voteAverage))
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundColor(.white)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(pillColor)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
        .padding(.vertical, 4)
    }

    private var posterPlaceholder: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.3))
            .frame(width: 80, height: 120)
            .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
