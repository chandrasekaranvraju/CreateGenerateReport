# CreateGenerateReport
This application extract raw data from API, Transform and Generate Report

**To run the application**

1. Pull repo from git hub using https://github.com/chandrasekaranvraju/CreateGenerateReport
2. Clone using git clone https://github.com/chandrasekaranvraju/CreateGenerateReport
3. Navigate to the project folder and perform below steps
   a. docker build -t create-report:1.0 .
   b. docker run -i create-report:1.0 .
   
   create-report -> Docker container name
   
 **Scheduler
 1. CreateReport Scheduler scheduled every 1 min
 2. GenerateReport Scheduler scheduled every 10 min
   
 **Output Files
 
   Output files will be created in below folders
   1. /output/data/ - This will contain all the raw data files that is extracted from API.
   2. /output/report/ - This will contain the aggregate report of the records having temperature of more than or equal to 45 pulled in previous 10 mins. 


**To verify the output

1. Navigate to output folder and check whether file is created inside the Data folder. The file will contain the transformed data retrieved from API.
2. Navigate to output folder and check whether aggregate report is created inside the report folder. The file will contain the aggregate data from previous files   
   generated before 10 minutes ago where temperature is equal and more than 45.
