import { getStreamingKeys } from "@/config/streamingConfig";

export interface YoutubeSearchResult {
  videoId: string;
  title: string;
  channelTitle: string;
  thumbnailUrl?: string;
}

export async function searchYoutubeVideos(query: string, maxResults = 5): Promise<YoutubeSearchResult[]> {
  const params = new URLSearchParams({
    part: "snippet",
    q: query,
    type: "video",
    videoCategoryId: "10", // Music
    maxResults: String(maxResults),
    key: getStreamingKeys().youtubeApiKey,
  });

  const res = await fetch(`https://www.googleapis.com/youtube/v3/search?${params.toString()}`);
  if (!res.ok) {
    throw new Error(`YouTube search failed: ${res.status} ${await res.text()}`);
  }

  const json = await res.json();
  const items: any[] = json.items ?? [];
  return items
    .filter((item) => item.id?.videoId)
    .map((item) => ({
      videoId: item.id.videoId,
      title: item.snippet?.title ?? "",
      channelTitle: item.snippet?.channelTitle ?? "",
      thumbnailUrl: item.snippet?.thumbnails?.default?.url,
    }));
}

/** One-shot convenience: find the single best-guess YouTube match for a Spotify track. */
export async function findBestYoutubeMatch(artist: string, title: string): Promise<YoutubeSearchResult | null> {
  const results = await searchYoutubeVideos(`${artist} - ${title} official audio`, 3);
  return results[0] ?? null;
}
