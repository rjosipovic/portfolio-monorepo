// Get the current year for the footer
document.getElementById('current-year').textContent = new Date().getFullYear();

// --- API Configuration ---
const PLACEHOLDER_IMAGE = 'https://placehold.co/400x600/6B7280/FFFFFF?text=No+Image';

// This is the base URL for your custom backend service.
const CUSTOM_API_BASE_URL = 'http://localhost:8080';

// --- Get DOM Elements ---
const navHome = document.getElementById('nav-home');
const navMovies = document.getElementById('nav-movies');
const navActors = document.getElementById('nav-actors');
const navDirectors = document.getElementById('nav-directors');
const navCharacters = document.getElementById('nav-characters');
const navAbout = document.getElementById('nav-about');

const homeView = document.getElementById('home-view');
const moviesView = document.getElementById('movies-view');
const actorsView = document.getElementById('actors-view');
const directorsView = document.getElementById('directors-view');
const charactersView = document.getElementById('characters-view');
const aboutView = document.getElementById('about-view');

// Detail Views
const movieDetailView = document.getElementById('movie-detail-view');
const actorDetailView = document.getElementById('actor-detail-view');
const directorDetailView = document.getElementById('director-detail-view');
const characterDetailView = document.getElementById('character-detail-view');

// Movie List Elements
const movieSearchInput = document.getElementById('movie-search-input');
const genreFilter = document.getElementById('genre-filter');
const movieGrid = document.getElementById('movie-grid');

// Actor List Elements
const actorSearchInput = document.getElementById('actor-search-input');
const actorGrid = document.getElementById('actor-grid');

// Director List Elements
const directorSearchInput = document.getElementById('director-search-input');
const directorGrid = document.getElementById('director-grid');

// Character List Elements
const characterSearchInput = document.getElementById('character-search-input');
const characterGrid = document.getElementById('character-grid');

// Home Page Buttons
const homeToMoviesBtn = document.getElementById('home-to-movies');
const homeToActorsBtn = document.getElementById('home-to-actors');
const homeToDirectorsBtn = document.getElementById('home-to-directors');
const homeToCharactersBtn = document.getElementById('home-to-characters');

// Detail View Buttons
const backToMoviesBtn = document.getElementById('back-to-movies-btn');
const backToActorsBtn = document.getElementById('back-to-actors-btn');
const backToDirectorsBtn = document.getElementById('back-to-directors-btn');
const backToCharactersBtn = document.getElementById('back-to-characters-btn');

// --- API Fetching Functions ---
/**
 * Placeholder function to fetch data from your custom backend.
 * You will need to build a backend service that responds to these endpoints.
 * @param {string} endpoint - The API endpoint (e.g., '/characters')
 * @param {object} params - Query parameters
 */
async function fetchFromCustomAPI(endpoint, params = {}) {
    const url = new URL(`${CUSTOM_API_BASE_URL}${endpoint}`);
    for (const key in params) {
        url.searchParams.append(key, params[key]);
    }

    try {
        const response = await fetch(url);
        if (!response.ok) {
            console.error(`Custom API Error: ${response.status} ${response.statusText} for endpoint ${endpoint}`);
            // For a real app, you might want to handle different statuses (404, 500, etc.)
            return null;
        }
        return await response.json();
    } catch (error) {
        console.error(`Failed to fetch from Custom API endpoint ${endpoint}:`, error);
        console.error("Please ensure your custom backend server is running and configured to handle this request.");
        return null;
    }
}

// --- Utility Functions ---
/**
 * Debounce function to limit the rate at which a function gets called.
 * @param {Function} func The function to debounce.
 * @param {number} delay The delay in milliseconds.
 */
function debounce(func, delay) {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
            func.apply(this, args);
        }, delay);
    };
}

// --- View Management ---
function showView(viewToShow) {
    // Hide all views
    document.querySelectorAll('.view-section').forEach(view => {
        view.classList.remove('active');
    });
    // Show the requested view
    viewToShow.classList.add('active');

    // Render content specific to the active view
    if (viewToShow === moviesView) {
        renderMovies();
    } else if (viewToShow === actorsView) {
        renderActors();
    } else if (viewToShow === directorsView) {
        renderDirectors();
    } else if (viewToShow === charactersView) {
        renderCharacters();
    }
    // Detail views, Home, and About views are rendered by specific functions or are static
}

// --- Movie Functions ---
function createMovieCard(movie) {
    const movieCard = document.createElement('div');
    movieCard.className = 'bg-gray-800 rounded-xl shadow-lg overflow-hidden transform transition duration-300 hover:scale-105 hover:shadow-2xl flex flex-col';

    const img = document.createElement('img');
    img.src = movie.imageUrl || PLACEHOLDER_IMAGE;
    img.alt = movie.title;
    img.className = 'w-full h-72 object-cover rounded-t-xl';
    img.onerror = function() {
        this.onerror = null;
        this.src = PLACEHOLDER_IMAGE;
    };
    movieCard.appendChild(img);

    const contentDiv = document.createElement('div');
    contentDiv.className = 'p-5 flex flex-col flex-grow';

    const title = document.createElement('h3');
    title.className = 'text-xl font-semibold text-white mb-2';
    title.textContent = movie.title;
    contentDiv.appendChild(title);

    const description = document.createElement('p');
    description.className = 'text-gray-300 text-sm flex-grow';
    description.textContent = movie.description;
    contentDiv.appendChild(description);

    const button = document.createElement('button');
    button.className = 'mt-4 bg-purple-600 hover:bg-purple-700 text-white font-bold py-2 px-4 rounded-full shadow-md transition duration-300 ease-in-out transform hover:-translate-y-1';
    button.textContent = 'View Details';
    button.addEventListener('click', () => {
        showMovieDetail(movie.id);
    });
    contentDiv.appendChild(button);

    movieCard.appendChild(contentDiv);
    return movieCard;
}

async function fetchMovies(searchTerm = '', genre = '') {
    const params = {};
    if (searchTerm) params.title = searchTerm;
    if (genre && genre !== 'all') params.genre = genre;
    return await fetchFromCustomAPI('/movies', params);
}

async function renderMovies() {
    if (!movieGrid) {
        console.error("Movie grid element not found!");
        return;
    }
    movieGrid.innerHTML = `<p class="text-center text-gray-400 text-lg col-span-full">Loading movies...</p>`;

    const searchTerm = movieSearchInput.value.toLowerCase();
    const selectedGenre = genreFilter.value;

    const movies = await fetchMovies(searchTerm, selectedGenre);

    movieGrid.innerHTML = '';

    if (!movies || movies.length === 0) {
        movieGrid.innerHTML = `
            <p class="text-center text-gray-400 text-lg col-span-full">No movies found. (Is your backend running?)</p>
        `;
    } else {
        movies.forEach(movie => {
            const card = createMovieCard(movie);
            movieGrid.appendChild(card);
        });
    }
}

async function showMovieDetail(movieId) {
    const movie = await fetchFromCustomAPI(`/movies/${movieId}`);
    if (movie) {
        document.getElementById('movie-detail-image').src = movie.imageUrl || PLACEHOLDER_IMAGE;
        document.getElementById('movie-detail-image').alt = movie.title;
        document.getElementById('movie-detail-title').textContent = movie.title;
        document.getElementById('movie-detail-genre').textContent = `Genre: ${movie.genre.charAt(0).toUpperCase() + movie.genre.slice(1)}`;
        document.getElementById('movie-detail-description').textContent = movie.fullDescription || movie.description;
        showView(movieDetailView);
    } else {
        console.error(`Movie with ID ${movieId} not found.`);
        alert('Could not load movie details.');
        showView(moviesView);
    }
}

// --- People (Actors & Directors) Functions ---
function createPersonCard(person, type) {
    const card = document.createElement('div');
    card.className = 'bg-gray-800 rounded-xl shadow-lg overflow-hidden transform transition duration-300 hover:scale-105 hover:shadow-2xl flex flex-col items-center text-center';

    const placeholderText = type.charAt(0).toUpperCase() + type.slice(1);
    const img = document.createElement('img');
    img.src = person.imageUrl || `https://placehold.co/128x128/6B7280/FFFFFF?text=${placeholderText}`;
    img.alt = person.name;
    
    const borderColor = type === 'actor' ? 'border-indigo-500' : 'border-emerald-500';
    img.className = `w-32 h-32 object-cover rounded-full mt-5 mb-3 border-4 ${borderColor}`;
    img.onerror = function() {
        this.onerror = null;
        this.src = `https://placehold.co/128x128/6B7280/FFFFFF?text=${placeholderText}`;
    };
    card.appendChild(img);

    const contentDiv = document.createElement('div');
    contentDiv.className = 'p-5 flex flex-col flex-grow';

    const name = document.createElement('h3');
    name.className = 'text-xl font-semibold text-white mb-2';
    name.textContent = person.name;
    contentDiv.appendChild(name);

    const bio = document.createElement('p');
    bio.className = 'text-gray-300 text-sm flex-grow';
    bio.textContent = person.bio;
    contentDiv.appendChild(bio);

    const button = document.createElement('button');
    const buttonColor = type === 'actor' ? 'bg-indigo-600 hover:bg-indigo-700' : 'bg-emerald-600 hover:bg-emerald-700';
    button.className = `mt-4 ${buttonColor} text-white font-bold py-2 px-4 rounded-full shadow-md transition duration-300 ease-in-out transform hover:-translate-y-1`;
    button.textContent = 'View Details';
    button.addEventListener('click', () => {
        if (type === 'actor') {
            showActorDetail(person.id);
        } else {
            showDirectorDetail(person.id);
        }
    });
    contentDiv.appendChild(button);

    card.appendChild(contentDiv);
    return card;
}

async function fetchPeople(type, searchTerm = '') {
    const endpoint = type === 'actor' ? '/actors' : '/directors';
    const params = searchTerm ? { name: searchTerm } : {};
    return await fetchFromCustomAPI(endpoint, params);
}

async function renderPeople(type) {
    const grid = type === 'actor' ? actorGrid : directorGrid;
    const searchInput = type === 'actor' ? actorSearchInput : directorSearchInput;
    
    if (!grid) {
        console.error(`${type} grid element not found!`);
        return;
    }
    grid.innerHTML = `<p class="text-center text-gray-400 text-lg col-span-full">Loading ${type}s...</p>`;

    const searchTerm = searchInput.value.toLowerCase();
    const people = await fetchPeople(type, searchTerm);

    grid.innerHTML = '';

    if (!people || people.length === 0) {
        grid.innerHTML = `
            <p class="text-center text-gray-400 text-lg col-span-full">No ${type}s found. (Is your backend running?)</p>
        `;
    } else {
        people.forEach(person => {
            const card = createPersonCard(person, type);
            grid.appendChild(card);
        });
    }
}

function renderActors() {
    renderPeople('actor');
}

function renderDirectors() {
    renderPeople('director');
}

async function showPersonDetail(personId, type) {
    const endpoint = type === 'actor' ? `/actors/${personId}` : `/directors/${personId}`;
    const person = await fetchFromCustomAPI(endpoint);

    if (person) {
        const detailView = type === 'actor' ? actorDetailView : directorDetailView;
        document.getElementById(`${type}-detail-image`).src = person.imageUrl || PLACEHOLDER_IMAGE;
        document.getElementById(`${type}-detail-image`).alt = person.name;
        document.getElementById(`${type}-detail-name`).textContent = person.name;
        document.getElementById(`${type}-detail-bio`).textContent = person.fullBio || person.bio;
        showView(detailView);
    } else {
        console.error(`${type} with ID ${personId} not found.`);
        alert(`Could not load ${type} details.`);
        showView(type === 'actor' ? actorsView : directorsView);
    }
}

function showActorDetail(actorId) {
    showPersonDetail(actorId, 'actor');
}

function showDirectorDetail(directorId) {
    showPersonDetail(directorId, 'director');
}

// --- Character Functions ---
function createCharacterCard(character) {
    const characterCard = document.createElement('div');
    characterCard.className = 'bg-gray-800 rounded-xl shadow-lg overflow-hidden transform transition duration-300 hover:scale-105 hover:shadow-2xl flex flex-col items-center text-center';

    const img = document.createElement('img');
    img.src = character.imageUrl || `https://placehold.co/128x128/6B7280/FFFFFF?text=Character`;
    img.alt = character.name;
    img.className = 'w-32 h-32 object-cover rounded-full mt-5 mb-3 border-4 border-yellow-500';
    img.onerror = function() {
        this.onerror = null;
        this.src = `https://placehold.co/128x128/6B7280/FFFFFF?text=Character`;
    };
    characterCard.appendChild(img);

    const contentDiv = document.createElement('div');
    contentDiv.className = 'p-5 flex flex-col flex-grow';

    const name = document.createElement('h3');
    name.className = 'text-xl font-semibold text-white mb-2';
    name.textContent = character.name;
    contentDiv.appendChild(name);

    const movie = document.createElement('p');
    movie.className = 'text-gray-400 text-sm mb-1';
    movie.textContent = `From: ${character.movie}`;
    contentDiv.appendChild(movie);

    const description = document.createElement('p');
    description.className = 'text-gray-300 text-sm flex-grow';
    description.textContent = character.description;
    contentDiv.appendChild(description);

    const button = document.createElement('button');
    button.className = 'mt-4 bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded-full shadow-md transition duration-300 ease-in-out transform hover:-translate-y-1';
    button.textContent = 'View Details';
    button.addEventListener('click', () => {
        showCharacterDetail(character.id);
    });
    contentDiv.appendChild(button);

    characterCard.appendChild(contentDiv);
    return characterCard;
}

async function fetchCharacters(searchTerm = '') {
    // This function calls your custom backend.
    // The endpoint '/characters' and the query param 'name' are placeholders.
    const params = searchTerm ? { name: searchTerm } : {};
    return await fetchFromCustomAPI('/characters', params);
}

async function renderCharacters() {
    if (!characterGrid) {
        console.error("Character grid element not found!");
        return;
    }
    characterGrid.innerHTML = `<p class="text-center text-gray-400 text-lg col-span-full">Loading characters...</p>`;

    const searchTerm = characterSearchInput.value.toLowerCase();
    const characters = await fetchCharacters(searchTerm);

    characterGrid.innerHTML = '';

    if (!characters || characters.length === 0) {
        characterGrid.innerHTML = `
            <p class="text-center text-gray-400 text-lg col-span-full">No characters found. (Is your backend running?)</p>
        `;
    } else {
        characters.forEach(character => {
            const card = createCharacterCard(character);
            characterGrid.appendChild(card);
        });
    }
}

async function showCharacterDetail(characterId) {
    // This function calls your custom backend for a single character.
    // The endpoint '/characters/${characterId}' is a placeholder.
    const character = await fetchFromCustomAPI(`/characters/${characterId}`);
    if (character) {
        document.getElementById('character-detail-image').src = character.imageUrl || PLACEHOLDER_IMAGE;
        document.getElementById('character-detail-image').alt = character.name;
        document.getElementById('character-detail-name').textContent = character.name;
        document.getElementById('character-detail-movie').textContent = `From: ${character.movie}`;
        document.getElementById('character-detail-description').textContent = character.fullDescription || character.description;
        showView(characterDetailView);
    } else {
        console.error(`Character with ID ${characterId} not found.`);
        alert('Could not load character details.');
        showView(charactersView);
    }
}

// --- Event Listeners ---
document.addEventListener('DOMContentLoaded', () => {
    // Initial view
    showView(homeView);

    // Navigation links
    navHome.addEventListener('click', (e) => { e.preventDefault(); showView(homeView); });
    navMovies.addEventListener('click', (e) => { e.preventDefault(); showView(moviesView); });
    navActors.addEventListener('click', (e) => { e.preventDefault(); showView(actorsView); });
    navDirectors.addEventListener('click', (e) => { e.preventDefault(); showView(directorsView); });
    navCharacters.addEventListener('click', (e) => { e.preventDefault(); showView(charactersView); });
    navAbout.addEventListener('click', (e) => { e.preventDefault(); showView(aboutView); });

    // Home page buttons
    homeToMoviesBtn.addEventListener('click', (e) => { e.preventDefault(); showView(moviesView); });
    homeToActorsBtn.addEventListener('click', (e) => { e.preventDefault(); showView(actorsView); });
    homeToDirectorsBtn.addEventListener('click', (e) => { e.preventDefault(); showView(directorsView); });
    homeToCharactersBtn.addEventListener('click', (e) => { e.preventDefault(); showView(charactersView); });

    // Back buttons for detail views
    backToMoviesBtn.addEventListener('click', () => showView(moviesView));
    backToActorsBtn.addEventListener('click', () => showView(actorsView));
    backToDirectorsBtn.addEventListener('click', () => showView(directorsView));
    backToCharactersBtn.addEventListener('click', () => showView(charactersView));

    const searchDebounceDelay = 300; // ms delay for search inputs

    // Movie search and filter
    movieSearchInput.addEventListener('input', debounce(renderMovies, searchDebounceDelay));
    genreFilter.addEventListener('change', renderMovies);

    // Actor search
    actorSearchInput.addEventListener('input', debounce(renderActors, searchDebounceDelay));

    // Director search
    directorSearchInput.addEventListener('input', debounce(renderDirectors, searchDebounceDelay));

    characterSearchInput.addEventListener('input', debounce(renderCharacters, searchDebounceDelay));
});
