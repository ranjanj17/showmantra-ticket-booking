package com.showmantra.mappers;

import com.showmantra.dtos.ShowtimeResponse;
import com.showmantra.entities.Show;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting a Show entity into a flattened ShowtimeResponse DTO.
 */
@Mapper(componentModel = "spring")
public interface ShowMapper {

    @Mapping(source = "id", target = "showId")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "screen.theater.name", target = "theaterName")
    @Mapping(source = "screen.name", target = "screenName")
    ShowtimeResponse toDto(Show show);
}
