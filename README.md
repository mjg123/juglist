# juglist

Some code to produce the Java User Group directory website

## How to?

  1. Clone this repo
  1. Set your `TWITTER_BEARER_TOKEN` env var ([how?](https://developer.twitter.com/en/docs/basics/authentication/guides/access-tokens.html))
  1. Run `lein run`
  1. Run `lein cljsbuild once min`
  
### What does this do?

  - The JSON representation of [Bruno Borges' list of JUGs](https://twitter.com/brunoborges/lists/jugs/members) is fetched, using your bearer token to authenticate to the Twitter API.
  - Each of those JUGs has its `location` field geocoded. There is a list of exceptions in the code for JUG accounts which are known to have a non-geocodable `location`, and some accounts which are active in multiple locations etc. Geocoding is performed by [OSM Nominatim](https://nominatim.openstreetmap.org/). Any JUGs which cannot be geolocated and have not been special cased will be logged with a warning.
  - In both steps above the results are cached into a directory called `cache` which will be created in the project root, so you can use `lein run` repeatedly without worrying about http rate limiting.
  - The output of this stage is some clojurescript source representing all the JUGs in `src-cljs/juglist/data.cljs`. The main ClojureScript source is in `src-cljs/juglist/page.cljs`, which will requrie `data.cljs`, and once that is present you can compile the whole shebang with `lein cljsbuild once min`
  - This puts a `juglist.js` file into the `web` folder, where there is already a `map.html`. Load these up in your browser, or move them over to your static host.
  - It is also set up for `lein figwheel` to work if you want to do live development of the UI.
