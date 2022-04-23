package aiss.api.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.jboss.resteasy.spi.NotFoundException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import aiss.model.Song;
import aiss.model.repository.MapPlaylistRepository;
import aiss.model.repository.PlaylistRepository;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;



@Path("/songs")
public class SongResource {

	public static SongResource _instance=null;
	PlaylistRepository repository;
	
	private SongResource(){
		repository=MapPlaylistRepository.getInstance();
	}
	
	public static SongResource getInstance()
	{
		if(_instance==null)
			_instance=new SongResource();
		return _instance; 
	}
	
	@GET
	@Produces("application/json")
	public Collection<Song> getAll(@QueryParam("q") String q, @QueryParam("order") String order, @QueryParam("limit") Integer limit, @QueryParam("offset") Integer offset)
	{
		List<Song> result = new ArrayList<>(), songs = repository.getAllSongs().stream().collect(Collectors.toList());
		int start = offset == null ? 0: offset-1;
		int end = limit == null ? songs.size(): start  + limit;
		
		if (q != null)
			for (int i = start; i < end; i++) {
				if (songs.get(i).getTitle().contains(q) 
						|| songs.get(i).getAlbum().contains(q) 
						|| songs.get(i).getArtist().contains(q))
					result.add(songs.get(i));
			}
		else 
			result = songs;
		
		if (order != null) {
			if (order.equals("album"))
				Collections.sort(result, Comparator.comparing(Song::getAlbum));
			else if (order.equals("-album"))
				Collections.sort(result, Comparator.comparing(Song::getAlbum).reversed());
			else if (order.equals("artist"))
				Collections.sort(result, Comparator.comparing(Song::getArtist));
			else if (order.equals("-artist"))
				Collections.sort(result, Comparator.comparing(Song::getArtist).reversed());
			if (order.equals("year"))
				Collections.sort(result, Comparator.comparing(Song::getYear));
			else if (order.equals("-year"))
				Collections.sort(result, Comparator.comparing(Song::getYear).reversed());
			
			
		}
		return result;
	}
	
	
	@GET
	@Path("/{id}")
	@Produces("application/json")
	public Song get(@PathParam("id") String songId)
	{
		Song song = repository.getSong(songId);
		
		if (song == null) {
			throw new NotFoundException("The song with id="+ songId +" was not found");
		}
		
		return song;
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response addSong(@Context UriInfo uriInfo, Song song) {
		if (song.getTitle() == null || "".equals(song.getTitle())) 
			throw new NotFoundException("The title of the song must not be null");
		
		repository.addSong(song);
		
		// Builds the response. Returns the playlist the has just been added.
		UriBuilder ub = uriInfo.getAbsolutePathBuilder().path(this.getClass(), "get");
		URI uri = ub.build(song.getId());
		ResponseBuilder resp = Response.created(uri);
		resp.entity(song);			
		return resp.build();
	}
	
	
	@PUT
	@Consumes("application/json")
	public Response updateSong(Song song) {
		
		Song oldsong = repository.getSong(song.getId());
		if (oldsong == null) {
			throw new NotFoundException("The song with id="+ song.getId() +" was not found");			
		}
		
		// Update title
		if (song.getTitle()!=null)
			oldsong.setTitle(song.getTitle());
		
		// Update artist
		if (song.getArtist() != null) 
			oldsong.setArtist(song.getArtist());
		
		// Update album
		if (song.getAlbum() != null)
			oldsong.setAlbum(song.getAlbum());
		
		// Update year
		if (song.getYear() != null) 
			oldsong.setYear(song.getYear());
		
		return Response.noContent().build();
	}
	
	@DELETE
	@Path("/{id}")
	public Response removeSong(@PathParam("id") String songId) {
		Song toberemoved=repository.getSong(songId);
		if (toberemoved == null)
			throw new NotFoundException("The song with id="+ songId +" was not found");
		else
			repository.deleteSong(songId);
		
		return Response.noContent().build();
	}
	
}
