package com.driver.services;


import com.driver.EntryDto.BookTicketEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.PassengerRepository;
import com.driver.repository.TicketRepository;
import com.driver.repository.TrainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    @Autowired
    TrainRepository trainRepository;

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    TrainService trainService;


    public Integer bookTicket(BookTicketEntryDto bookTicketEntryDto)throws Exception{
     Train train = trainRepository.findById(bookTicketEntryDto.getTrainId()).get();
     HashMap<String,Integer> hm  = trainService.routeToHashMap(train.getRoute());
//    for(String x:hm.keySet()){
//        System.out.println(x);
//    }
     if((!hm.containsKey(bookTicketEntryDto.getFromStation().name())) || (!hm.containsKey(bookTicketEntryDto.getToStation().name()))){
         throw new Exception("Invalid stations");
     }

     SeatAvailabilityEntryDto seatAvailabilityEntryDto = new SeatAvailabilityEntryDto(bookTicketEntryDto.getTrainId(),bookTicketEntryDto.getFromStation(),bookTicketEntryDto.getToStation());
     int SeatsAvailable = trainService.calculateAvailableSeats(seatAvailabilityEntryDto);
     int updateSeats = SeatsAvailable-bookTicketEntryDto.getNoOfSeats();
     if(updateSeats<=0){
          throw new Exception("Less tickets are available");
     }
     int btwStations = hm.get(bookTicketEntryDto.getToStation().name())-hm.get(bookTicketEntryDto.getFromStation().name())+1;
     int totalfare = ((btwStations-1)*300);

     List<Integer> passengerId = bookTicketEntryDto.getPassengerIds();
     List<Passenger> passengers = new ArrayList<>();
     for (Integer x:passengerId){
          passengers.add(passengerRepository.findById(x).get());
     }
     Ticket ticket = new Ticket(0,passengers,train,bookTicketEntryDto.getFromStation(),bookTicketEntryDto.getToStation(),totalfare);


     Passenger passenger = passengerRepository.findById(bookTicketEntryDto.getBookingPersonId()).get();
     passenger.getBookedTickets().add(ticket);
     train.getBookedTickets().add(ticket);
     Ticket savedTicket = ticketRepository.save(ticket);

        //Check for validity
        //Use bookedTickets List from the TrainRepository to get bookings done against that train
        // Incase the there are insufficient tickets
        // throw new Exception("Less tickets are available");
        //otherwise book the ticket, calculate the price and other details
        //Save the information in corresponding DB Tables
        //Fare System : Check problem statement
        //Incase the train doesn't pass through the requested stations
        //throw new Exception("Invalid stations");
        //Save the bookedTickets in the train Object
        //Also in the passenger Entity change the attribute bookedTickets by using the attribute bookingPersonId.
       //And the end return the ticketId that has come from db

        return savedTicket.getTicketId();

    }
}
