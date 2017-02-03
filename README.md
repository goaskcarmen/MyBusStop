# MyBusStop
MyBusStop is a capstone project as part of the Ada Developers Academy curriculum. 
## Purpose
MyBusStop is an Android application that alert public transit passengers when they are within a certain distance (~1 mile) from their destination.

###Market Opportunity
- Public transit travelers often miss their bus or train stop due to reasons such as digital distraction, sleeping, reading, or going to an unfamiliar place. 
- A study found that approximately 20 million public transit travelers miss their bus or train stop every year.( http://www.dailymail.co.uk/sciencetech/article-2341068/Digital-distraction-causes-20-million-passengers-miss-bus-train-stop-year.html)
    - 51% have been caused by digital distraction
    - 15% have been late to meetings because they missed their stop
    - 3 in 5 people miss their stop per year
    
###Defining a Project
- Define a NEED statement
    - Solution free 
    - Must be certain, without ambiguity
    - If any part of the “need” statement cannot be satisfied the entire project fails
- In this project, the “need” statement is:  
  **“Prevent public transit travelers from missing their destination stops.”**
  
###Potential Concept Ideas
1. Install a system that acquire travelers to enter their stop when they enter the vehicle and count the number of passengers that need to get off the vehicle. (Expensive and unreliable with human link) 
2. Install a system to announce the next stop on the bus/train. (Expensive)
3. Use a timer that everyone has on their phones. (Not effective)
4. Develop an app that alert passengers when they are within a certain distance from their destination. (The choosen concept)

###Stakeholder Requirements
- Common Users (active stakeholder/user)
    - Want to use a map to search for a destination
    - Want to be notified about 2 stops before the destination
- Frequent Traveler (active stakeholder/user)
    - Want to save their frequent bus/train stop
- Tired Traveler (active stakeholder/user)
    - Want to be able to set the volume of the alarm
- Traveler who is on the phone (active stakeholder/user)
    - Want to get notification via text
    - Want to be able to save their phone battery
- Traveler who is reading (active stakeholder/user)
    - Want to be notified via alarm and/or vibration
- User who travels to the location for the first time (active stakeholder/user)
    - Want to get notification for multiple stops, if any
- Imaginary investor (active stakeholder)
- PM (active stakeholder)
    - On schedule 
- Googlemaps API (passive stakeholder)
    - 1000 API calls per day
    
###Functionalities
- Current functionalities:
    - Search a destination on map
    - Zoom in/out
    - Satellite/street view
    - Notification is triggered within 1 mile from destination
    - Notification via text and/or alarm sound
    - Turn off notification
- Improvements: 
    - User log in to save frequent destination
    - Get notification for multiple stops
    - Battery efficient – more frequent GPS call only when the user is close to the destination
    - Automatically detect the closest bus stop from the destination, that is, if the destination is home and the closest bus stop is 2 blocks away, the app will notify the user based on the bus stop instead of home address.
    

