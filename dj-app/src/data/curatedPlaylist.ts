/**
 * Reference list of widely-considered essential songs per decade, 1960s-2010s.
 * Metadata only (title/artist/year/genre/approximate BPM) - deliberately no
 * audio. We don't have rights to bundle commercial recordings, and this repo's
 * whole premise is "everything runs locally, nothing downloaded from the
 * cloud." Users link each entry to a file they already legally own via the
 * "Suggestions" screen; BPM values here are commonly-cited approximations,
 * meant as a starting point - confirm with TAP BPM once real audio is loaded.
 */
export interface CuratedTrack {
  id: string;
  title: string;
  artist: string;
  year: number;
  decade: string;
  genre: string;
  approxBpm: number;
}

export const CURATED_PLAYLIST: CuratedTrack[] = [
  // 1960s
  { id: "c-1965-dylan", title: "Like a Rolling Stone", artist: "Bob Dylan", year: 1965, decade: "1960s", genre: "Rock", approxBpm: 95 },
  { id: "c-1965-brown", title: "I Got You (I Feel Good)", artist: "James Brown", year: 1965, decade: "1960s", genre: "Funk/Soul", approxBpm: 112 },
  { id: "c-1966-beachboys", title: "Good Vibrations", artist: "The Beach Boys", year: 1966, decade: "1960s", genre: "Pop/Rock", approxBpm: 120 },
  { id: "c-1967-aretha", title: "Respect", artist: "Aretha Franklin", year: 1967, decade: "1960s", genre: "Soul", approxBpm: 115 },
  { id: "c-1968-beatles", title: "Hey Jude", artist: "The Beatles", year: 1968, decade: "1960s", genre: "Pop/Rock", approxBpm: 74 },

  // 1970s
  { id: "c-1971-zeppelin", title: "Stairway to Heaven", artist: "Led Zeppelin", year: 1971, decade: "1970s", genre: "Rock", approxBpm: 82 },
  { id: "c-1975-queen", title: "Bohemian Rhapsody", artist: "Queen", year: 1975, decade: "1970s", genre: "Rock", approxBpm: 72 },
  { id: "c-1977-beegees", title: "Stayin' Alive", artist: "Bee Gees", year: 1977, decade: "1970s", genre: "Disco", approxBpm: 103 },
  { id: "c-1977-summer", title: "I Feel Love", artist: "Donna Summer", year: 1977, decade: "1970s", genre: "Disco/Electronic", approxBpm: 125 },
  { id: "c-1978-chic", title: "Le Freak", artist: "Chic", year: 1978, decade: "1970s", genre: "Disco", approxBpm: 110 },

  // 1980s
  { id: "c-1983-mj", title: "Billie Jean", artist: "Michael Jackson", year: 1983, decade: "1980s", genre: "Pop", approxBpm: 117 },
  { id: "c-1983-neworder", title: "Blue Monday", artist: "New Order", year: 1983, decade: "1980s", genre: "Electronic", approxBpm: 130 },
  { id: "c-1984-prince", title: "When Doves Cry", artist: "Prince", year: 1984, decade: "1980s", genre: "Pop/Funk", approxBpm: 112 },
  { id: "c-1985-aha", title: "Take On Me", artist: "a-ha", year: 1985, decade: "1980s", genre: "Synth-pop", approxBpm: 169 },
  { id: "c-1989-madonna", title: "Like a Prayer", artist: "Madonna", year: 1989, decade: "1980s", genre: "Pop", approxBpm: 112 },

  // 1990s
  { id: "c-1991-nirvana", title: "Smells Like Teen Spirit", artist: "Nirvana", year: 1991, decade: "1990s", genre: "Grunge/Rock", approxBpm: 117 },
  { id: "c-1992-whitney", title: "I Will Always Love You", artist: "Whitney Houston", year: 1992, decade: "1990s", genre: "Pop/Ballad", approxBpm: 68 },
  { id: "c-1994-biggie", title: "Juicy", artist: "The Notorious B.I.G.", year: 1994, decade: "1990s", genre: "Hip-Hop", approxBpm: 95 },
  { id: "c-1997-daftpunk", title: "Around the World", artist: "Daft Punk", year: 1997, decade: "1990s", genre: "House/Electronic", approxBpm: 121 },
  { id: "c-1999-tlc", title: "No Scrubs", artist: "TLC", year: 1999, decade: "1990s", genre: "R&B", approxBpm: 93 },

  // 2000s
  { id: "c-2000-daftpunk", title: "One More Time", artist: "Daft Punk", year: 2000, decade: "2000s", genre: "House", approxBpm: 123 },
  { id: "c-2003-outkast", title: "Hey Ya!", artist: "OutKast", year: 2003, decade: "2000s", genre: "Hip-Hop/Pop", approxBpm: 160 },
  { id: "c-2003-beyonce", title: "Crazy in Love", artist: "Beyoncé ft. Jay-Z", year: 2003, decade: "2000s", genre: "R&B/Pop", approxBpm: 99 },
  { id: "c-2006-winehouse", title: "Rehab", artist: "Amy Winehouse", year: 2006, decade: "2000s", genre: "Soul", approxBpm: 139 },
  { id: "c-2007-rihanna", title: "Umbrella", artist: "Rihanna ft. Jay-Z", year: 2007, decade: "2000s", genre: "Pop/R&B", approxBpm: 174 },

  // 2010s
  { id: "c-2011-adele", title: "Rolling in the Deep", artist: "Adele", year: 2011, decade: "2010s", genre: "Pop/Soul", approxBpm: 105 },
  { id: "c-2013-daftpunk", title: "Get Lucky", artist: "Daft Punk ft. Pharrell Williams", year: 2013, decade: "2010s", genre: "Disco/Funk", approxBpm: 116 },
  { id: "c-2013-avicii", title: "Wake Me Up", artist: "Avicii ft. Aloe Blacc", year: 2013, decade: "2010s", genre: "EDM/Folktronica", approxBpm: 124 },
  { id: "c-2014-ronson", title: "Uptown Funk", artist: "Mark Ronson ft. Bruno Mars", year: 2014, decade: "2010s", genre: "Funk/Pop", approxBpm: 115 },
  { id: "c-2019-weeknd", title: "Blinding Lights", artist: "The Weeknd", year: 2019, decade: "2010s", genre: "Synth-pop", approxBpm: 171 },
];

export const DECADES = ["1960s", "1970s", "1980s", "1990s", "2000s", "2010s"] as const;
