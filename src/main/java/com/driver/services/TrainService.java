package com.driver.services;

import com.driver.EntryDto.AddTrainEntryDto;
import com.driver.EntryDto.SeatAvailabilityEntryDto;
import com.driver.model.Passenger;
import com.driver.model.Station;
import com.driver.model.Ticket;
import com.driver.model.Train;
import com.driver.repository.TrainRepository;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.time.LocalTime;
import java.util.*;

@Service
public class TrainService {

    @Autowired
    TrainRepository trainRepository;

    public Integer addTrain(AddTrainEntryDto trainEntryDto){

        String route = "";
        List<Station> stationList = trainEntryDto.getStationRoute();
        for(int i=0;i<stationList.size();i++){
            if(i==stationList.size()-1){
                route+=stationList.get(i);
            }
            else{
                route += stationList.get(i) + ",";
            }
        }
        Train train = new Train(route,trainEntryDto.getDepartureTime(),trainEntryDto.getNoOfSeats());

        Train savedtrain = trainRepository.save(train);
        //Add the train to the trainRepository
        //and route String logic to be taken from the Problem statement.
        //Save the train and return the trainId that is generated from the database.
        //Avoid using the lombok library
        return savedtrain.getTrainId();
    }

    public Integer calculateAvailableSeats(SeatAvailabilityEntryDto seatAvailabilityEntryDto){
          Train train= trainRepository.findById(seatAvailabilityEntryDto.getTrainId()).get();
          int total = train.getNoOfSeats();
           HashMap<String,Integer> hm = routeToHashMap(train.getRoute());
           int strt = hm.get(seatAvailabilityEntryDto.getFromStation().name());
           int end = hm.get(seatAvailabilityEntryDto.getToStation().name());
           int filledSeat =0;
            for(Ticket ticket: train.getBookedTickets()){
            if((hm.get(ticket.getFromStation().name())>=strt && hm.get(ticket.getFromStation().name())<end)
                    || (hm.get(ticket.getToStation().name())<=end && hm.get(ticket.getToStation().name())>strt)
           || (hm.get(ticket.getFromStation().name())<=strt && hm.get(ticket.getToStation().name())>=end)){
                filledSeat+=ticket.getPassengersList().size();
            }
        }
        return total-filledSeat;
        //Calculate the total seats available
        //Suppose the route is A B C D
        //And there are 2 seats avaialble in total in the train
        //and 2 tickets are booked from A to C and B to D.
        //The seat is available only between A to C and A to B. If a seat is empty between 2 station it will be counted to our final ans
        //even if that seat is booked post the destStation or before the boardingStation
        //Inshort : a train has totalNo of seats and there are tickets from and to different locations
        //We need to find out the available seats between the given 2 stations.
    }

    public HashMap<String, Integer> routeToHashMap(String route) {
        HashMap<String,Integer> hm = new HashMap<>();
        String arr[] = route.split(",");
        for(int i=0;i< arr.length;i++) {
            hm.put(arr[i], i);
        }
        return hm;
    }

    public Integer calculatePeopleBoardingAtAStation(Integer trainId,Station station) throws Exception{

        Train train = trainRepository.findById(trainId).get();

        String route = train.getRoute();

        HashMap<String, Integer> hm = routeToHashMap(route);

        if(!hm.containsKey(station.name())){
            throw  new Exception("Train is not passing from this station");
        }
        int ans=0;
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket:tickets){
            if(ticket.getFromStation().equals(station)){
                ans+=ticket.getPassengersList().size();
            }
        }
        //We need to find out the number of people who will be boarding a train from a particular station
        //if the trainId is not passing through that station
        //throw new Exception("Train is not passing from this station");
        //  in a happy case we need to find out the number of such people.

        return ans;
    }

    public Integer calculateOldestPersonTravelling(Integer trainId){

        Train train = trainRepository.findById(trainId).get();
        if(train.getBookedTickets().size()==0){
            return 0;
        }
        int max = Integer.MIN_VALUE;
        List<Ticket> tickets = train.getBookedTickets();
        for(Ticket ticket:tickets){
            List<Passenger> passengers = ticket.getPassengersList();
            for(Passenger passenger: passengers){
                if(passenger.getAge()>max){
                    max=passenger.getAge();
                }
            }
        }
        //Throughout the journey of the train between any 2 stations
        //We need to find out the age of the oldest person that is travelling the train
        //If there are no people travelling in that train you can return 0
        return max;
    }

    public List<Integer> trainsBetweenAGivenTime(Station station, LocalTime startTime, LocalTime endTime){
     List<Train> trainList = trainRepository.findAll();

     int strtMin = (startTime.getHour()*60)+startTime.getMinute();
     int endMin =(endTime.getHour()*60)+endTime.getMinute();
     List<Integer> ans = new ArrayList<>();
     for(Train train:trainList){
         HashMap<String,Integer> hm = routeToHashMap(train.getRoute());
         if(!hm.containsKey(station.name())) continue;
       LocalTime traintime=train.getDepartureTime();
       int departTime = traintime.getHour()*60+traintime.getMinute();
           int reachTime = (hm.get(station.name())*60)+departTime;
           if(reachTime>=strtMin && reachTime<=endMin){
               ans.add(train.getTrainId());
           }
     }
        //When you are at a particular station you need to find out the number of trains that will pass through a given station
        //between a particular time frame both start time and end time included.
        //You can assume that the date change doesn't need to be done ie the travel will certainly happen with the same date (More details
        //in problem statement)
        //You can also assume the seconds and milli seconds value will be 0 in a LocalTime format.
        return ans;
    }

}
