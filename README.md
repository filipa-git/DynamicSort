# DynamicSort
Program exploring potential for dynamic sorting utilizing multiple sorting algorithms dependant on factors calculatable during run-time.

DynSort includes main algorithm for sorting as well as other algortihms used.

Sorting algorithms include:
- Insertion sort
- Quicksort
- Radix sort
- K-way merge sort
  
Factors include:
- Size of list
- Partial standard deviation for low/high lists used in Quicksort

The main program utilizes parallel execution between multiple hosts to accomodate large quantities of data.

DataProviderServiceImpl implements a host (called data provider) responsible for providing initial data and checking that data has been sorted.
Server implements a host (called server) responsible for merging sorted data and uploading to the data provider.
Sorter implements a host (called sorter) responsible for sorting data from the data provider and uploading it to the server.

This project was done as a masters project at Umea University, spring 2023.
