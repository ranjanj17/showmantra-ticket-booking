package com.showmantra.mappers;

import com.showmantra.dtos.SeatInfo;
import com.showmantra.entities.ShowSeat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting a ShowSeat entity into a SeatInfo DTO, 
 * abstracting away the physical Seat details.
 */
@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "seat.id", target = "seatId")
    @Mapping(source = "seat.rowNumber", target = "rowNumber")
    @Mapping(source = "seat.seatNumber", target = "seatNumber")
    @Mapping(source = "seat.seatClass", target = "seatClass")
    @Mapping(source = "status", target = "status")
    SeatInfo toDto(ShowSeat showSeat);
}
