# My Watchlist
This is a demo app that lets you create your own watchlist. You can browse and find all movies and TV shows from [The Movie Database ](https://www.themoviedb.org/ "The Movie Database").

This is a hobby project of mine that I'm using to both show and continue honing my skills as an Android developer.

The app should always be launchable and working in the *main* branch, but this is still a Work-In-Progress project. Features / stuff I'm working on next:
- [x] Collapsing TopAppBar for the Details screen. :heavy_check_mark: (2023.01.19) 
- [x] Refactoring bits of the Data layer to have a single methods for fetching Title details. :heavy_check_mark: (2023.01.19) 
- [x] Implementing internet connectivity checks. :heavy_check_mark: (2023.01.20) 
- [x] Download Genres list from API only once per day. Implementation with DataStore Preferences. :heavy_check_mark: (2023.01.20) 
- [x] Download images configuration periodically from Api using WorkManager. :heavy_check_mark: (2023.01.22) 
- [ ] Structural Changes:
	- [ ]	Change first Top destination to “Discover” and third to “Watchlist”
	- [ ]	Discover screen:
		- [ ]	Add Trending TV + Movies
		- [ ]	Add Upcoming Movies
		- [ ]	Add Top Rated Movies
		- [ ]	Add Popular Movies + TV
	- [ ]	Search screen:
		- [ ]	Add Filter (Genres, Years, Score, Movie/TV)
		- [ ]	Add pre-search content: e.g. recent searches, popular by genre etc. Content still TBD
	- [ ]	Watchlist Screen: 
		- [ ]	Add Filter (Genres, Years, Score, Movie/TV)
	- [ ]	Title List Screen:
		- [ ]	Prepare screen for viewing full any type of list. 
	- [ ]	Details Screen:
		- [x] 	Add Videos section. :heavy_check_mark: (2023.01.27) 
		- [x]	Add Recommended Movies section. :heavy_check_mark: (2023.01.31)
		- [x]	Add Similar Movies section. :heavy_check_mark: (2023.01.31)
		- [ ]	Add Reviews section
		- [x] 	Add more Extra Details: budget, profit, languages. :heavy_check_mark: (2023.01.31)
- [ ] Adapt for different screen sizes. 
- [ ] Better and more appropriate transitions between screens
- [ ] Caching of Search and Trending data and getting that data via flows.
- [ ] Possible theme change.

## Light Theme

<img src="https://user-images.githubusercontent.com/34347984/212759648-903e3147-7f9e-4968-9157-9e17c34b5890.jpg" width="250"/><img src="https://user-images.githubusercontent.com/34347984/212759652-24d1ff71-e1b6-4a66-afbd-84f7acf22e03.jpg" width="250"/><img src="https://user-images.githubusercontent.com/34347984/212759654-89df1db2-86fa-44fe-8d12-3920741d41da.jpg" width="250"/>

## Dark Theme

<img src="https://user-images.githubusercontent.com/34347984/212759656-f43ab28d-1bcb-49a1-9d6e-c1ca95c9b675.jpg" width="250"/><img src="https://user-images.githubusercontent.com/34347984/212759657-208b6432-0a00-4edf-bd7f-94dab7c96bc0.jpg" width="250"/><img src="https://user-images.githubusercontent.com/34347984/212759660-ede99931-6aac-4f32-86c8-be35219c9b5a.jpg" width="250"/>
