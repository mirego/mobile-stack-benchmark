import SwiftUI
import shared

@MainActor
class MovieDetailObservable: ObservableObject {
    @Published var uiState: MovieDetailUiState

    private let viewModel: MovieDetailViewModel
    private var observer: MovieDetailStateObserver?

    init(movieId: Int32) {
        let helper = KoinHelper()
        self.viewModel = helper.getMovieDetailViewModel(movieId: movieId)
        self.uiState = viewModel.uiState.value as! MovieDetailUiState
        observeState()
    }

    private func observeState() {
        observer = MovieDetailStateObserver(viewModel: viewModel) { [weak self] state in
            DispatchQueue.main.async {
                self?.uiState = state
            }
        }
    }

    func retry() {
        viewModel.retry()
    }

    deinit {
        observer?.close()
    }
}

struct MovieDetailView: View {
    let movieId: Int32

    @StateObject private var observable: MovieDetailObservable

    init(movieId: Int32) {
        self.movieId = movieId
        _observable = StateObject(wrappedValue: MovieDetailObservable(movieId: movieId))
    }

    var body: some View {
        Group {
            if observable.uiState.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let error = observable.uiState.error {
                VStack(spacing: 16) {
                    Text(error)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                    Button("Retry") {
                        observable.retry()
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if let movie = observable.uiState.movieDetail {
                MovieDetailContent(movie: movie)
            }
        }
        .navigationTitle("KMP-Native")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct MovieDetailContent: View {
    let movie: MovieDetail

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 0) {
                ZStack(alignment: .bottomLeading) {
                    if let backdropPath = movie.backdropPath {
                        AsyncImage(url: URL(string: "https://image.tmdb.org/t/p/w780\(backdropPath)")) { phase in
                            switch phase {
                            case .success(let image):
                                image
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                            case .failure:
                                backdropPlaceholder
                            case .empty:
                                ProgressView()
                                    .frame(maxWidth: .infinity)
                            @unknown default:
                                backdropPlaceholder
                            }
                        }
                        .frame(height: 220)
                        .clipped()
                    } else {
                        backdropPlaceholder
                    }

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
                                    .frame(width: 120, height: 180)
                            @unknown default:
                                posterPlaceholder
                            }
                        }
                        .frame(width: 120, height: 180)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .shadow(radius: 4)
                        .padding(.leading, 16)
                        .offset(y: 90)
                    }
                }

                Spacer().frame(height: 100)

                VStack(alignment: .leading, spacing: 8) {
                    Text(movie.title)
                        .font(.title2)
                        .fontWeight(.bold)

                    if let tagline = movie.tagline, !tagline.isEmpty {
                        Text(tagline)
                            .font(.subheadline)
                            .italic()
                            .foregroundColor(.secondary)
                    }

                    HStack {
                        Text("★ \(String(format: "%.1f", movie.voteAverage))")
                            .fontWeight(.bold)
                            .foregroundColor(.orange)

                        if let runtime = movie.runtime {
                            Text("•")
                                .foregroundColor(.secondary)
                            Text("\(runtime) min")
                                .foregroundColor(.secondary)
                        }

                        Text("•")
                            .foregroundColor(.secondary)
                        Text(movie.releaseDate)
                            .foregroundColor(.secondary)
                    }
                    .font(.subheadline)

                    if !movie.genres.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 8) {
                                ForEach(movie.genres, id: \.id) { genre in
                                    Text(genre.name)
                                        .font(.caption)
                                        .padding(.horizontal, 12)
                                        .padding(.vertical, 6)
                                        .background(Color.gray.opacity(0.2))
                                        .clipShape(Capsule())
                                }
                            }
                        }
                    }

                    Spacer().frame(height: 8)

                    Text(movie.overview)
                        .font(.body)
                }
                .padding(.horizontal, 16)

                Spacer().frame(height: 24)
            }
        }
    }

    private var backdropPlaceholder: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.3))
            .frame(height: 220)
    }

    private var posterPlaceholder: some View {
        Rectangle()
            .fill(Color.gray.opacity(0.3))
            .frame(width: 120, height: 180)
            .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
