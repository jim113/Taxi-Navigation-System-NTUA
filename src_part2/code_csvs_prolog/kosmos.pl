:- dynamic likes/2.
:- dynamic client/8.
:- dynamic taxi/7.
:- dynamic driver_speaks/2.

permittedWayType(living_street).
permittedWayType(motorway).
permittedWayType(motorway_link).
permittedWayType(primary).
permittedWayType(primary_link).
permittedWayType(secondary).
permittedWayType(secondary_link).
permittedWayType(tertiary).
permittedWayType(tertiary_link).
permittedWayType(residential).
permittedWayType(trunk).
permittedWayType(trunk_link).
permittedWayType(unknown).
permittedWayType(unclassified).

isEqual(A,A).

canMoveFromTo(Ax, Ay, Bx, By) :-
    node(Ax, Ay, LineID),
    node(Bx, By, LineID),
    true.

score(living_street, 0.95).

score(residential, 0.70).

score(tertiary, 0.65).
score(tertiary_link, 0.63).

score(secondary, 0.55).
score(secondary_link, 0.55).

score(primary, 0.35).
score(primary_link, 0.35).

score(trunk, 0.5).
score(trunk_link, 0.5).
score(motorway, 0.5).
score(motorway_link, 0.45).

score(_, 0.7).


highwayRank(LineID, Value) :- 
    lineSpecs(LineID, Type, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _), 
    score(Type, Score),
    Value = Score.

highwayRank1(LineID, Value) :- 
    lineSpecs(LineID, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, yes),
    Value = 0.8.
highwayRank1(_, 0.2).

highwayRank2(LineID, Value) :- 
    lineSpecs(LineID, _, _, _, _, Speed, _, _, _, _, _, _, _, _, _, _, _),
    (Speed < 40 -> Score is 0.7;
    Score is 0.2),
    Value = Score.
highwayRank2(_, 0.4).

highwayRank3(LineID, Value) :- 
    lineSpecs(LineID, _, _, _, Lanes, _, _, _, _, _, _, _, _, _, _, _, _),
    (Lanes < 2 -> Score is 0.8;
    Score is 0.5),
    Value = Score.
highwayRank3(_, 0.65).

trafficValueToRank(high, 1).
trafficValueToRank(medium, 0.8).
trafficValueToRank(low, 0.5).

trafficRank(LineID, Rank) :-
    client(_, _, _, _, Time, _, _, _),
    lineTraffic(LineID, StartTime, EndTime, Value),
    StartTime =< Time,
    EndTime >= Time,
    trafficValueToRank(Value, Rank).


trafficRank(_, 0.8).

weightFactor(Ax, Ay, Bx, By, Value) :-
    node(Ax, Ay, LineID),
    node(Bx, By, LineID),
    highwayRank(LineID, HRank),
    trafficRank(LineID, TRank),
    highwayRank1(LineID, HRank1),
    highwayRank2(LineID, HRank2),
    highwayRank3(LineID, HRank3),
    Value is HRank * TRank * HRank1 * HRank2 * HRank3.


luggageIn(NumberOfLuggage, DriverID) :-
    (NumberOfLuggage > 2 ->
    taxi(_, _, DriverID, _, _, _, large);
    taxi(_, _, DriverID, _, _, _, _),
    true).

isQualifiedDriverForClient(DriverID) :-
    client(_, _, _, _, _, _, Language, _),
    speaksDriver(DriverID, Language),
    taxi(_, _, DriverID, yes, _, _, _),
    client(_, _, _, _, _, Number, _, _),
    maxPassengers(DriverID, Max),
    Number =< Max,
    client(_, _, _, _, _, _, _,NumberOfLuggage),
    luggageIn(NumberOfLuggage, DriverID).

driverRank(DriverID, Rank) :-
    taxi(_, _, DriverID, _, Rank, _, _).
