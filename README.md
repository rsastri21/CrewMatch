# LUX CrewMatch

*If you are a LUX Officer or Member wanting to report feedback, please contact Rohan Sastri via Slack.*

This repository contains the backend code for the API layer of the CrewMatch application.
The project is written in Java and built with the Spring Boot Web framework. 

---

## API Endpoints

The API contains requests categorized between two entities: *Candidates* and *Productions*.

**Update: *Headers* and *User* have been added with respective functionalities.**

`Heroku is being used as the deployment service. Heroku PostgreSQL is used as the database. For security purposes, the URL of the API will not be distributed.`

---

### Candidate Endpoints

Base level access point: `/api/candidate`

*All other endpoints extend from this base URL.*

| URL     | Request Type | Function |
| ------- | :----------: | -------- |
| `/get` | **GET** | Gets all candidates |
| `/getCount` | **GET** | Gets the count of candidates |
| `/get/{id}` | **GET** | Gets a candidate by ID |
| `/get/percentAssigned` | **GET** | Gets the percentage of candidates assigned to a crew | 
| `/get/percentActing` | **GET** | Gets the percentage of candidates interested in acting |
| `/search` | **GET** | Searches for candidates by assigned boolean and/or by actingInterest boolean |
| `/add` | **POST** | Creates a new candidate with parameters specified in request body |
| `/upload` | **POST** | Creates candidates in bulk from a CSV file specified in request body |
| `/update/{id}` | **PUT** | Updates a candidate by ID with parameters specified in request body |
| `/delete/{id}` | **DELETE** | Deletes a candidate by ID |

---

### Production Endpoints

Base level access point: `/api/production`

*All other endpoints extend from this base URL.*

| URL     | Request Type | Function |
| ------- | :----------: | -------- |
| `/get` | **GET** | Gets all productions |
| `/get/{id}` | **GET** | Gets a production by ID | 
| `/get/roles` | **GET** | Gets all the roles contained in productions. |
| `/match` | **GET** | Matches candidates to productions according to preferences |
| `/matchNoPreference` | **GET** | Matches candidates without strictly following preferences |
| `/search`  | **GET** | Searches for productions by name |
| `/create` | **POST** | Creates a new production with parameters specified in request body |
| `/assign/{productionID}/{candidateID}/{roleIndex}` | **PUT** | Manually assigns a candidate to a particular role in a production |
| `/update/{id}` | **PUT** | Updates a production by ID with parameters specified in request body |
| `/swap` | **PUT** | Swaps members between two productions with a SwapRequest request body | 
| `/delete/{id}` | **DELETE** | Deletes a production by ID |

---

### User Endpoints

Base level access point: `/api/user`

*All other endpoints extend from this base URL.*

| URL    | Request Type | Function | 
| ------ | :-----------:| -------- |
| `/get` | **GET** | Gets all registered users. |
| `/register` | **POST** | Creates a new user with default permissions with a username and password set in the request body. |
| `/login` | **POST** | Authenticates a user and returns their permission level. |
| `/update` | **PUT** | Updates a user's role. |
| `/delete/{id}` | **DELETE** | Deletes a user by ID. |
| `/deleteAll` | **DELETE** | Deletes all the registered users. | 

#### Usage

The user controller serves as the in-built authentication and authorization service. There are 3 tiers of permissions levels: user, production head, and admin. 

When a user is registered, they are automatically configured with the base-level permissions. These permissions can be updated by someone who has admin privileges.

### Some Notes on Security
* The API is protected via CORS to restrict requests, so user data cannot be accessed from unauthorized systems.
* Passwords are stored using the BCrypt hashing algorithm and neither the frontend nor backend store password related information in plain-text.
* The hash used is not feasibly reversible, keeping information secure. 

---

### Header Endpoints

Base level access point: `/api/headers`

*All other endpoints extend from this base URL.*

| URL    | Request Type | Function |
| ------ | :----------: | -------- |
| `/get` | **GET** | Gets the current CSV Headers |
| `/update` | **POST** | Updates the CSV Headers or creates new headers |

#### Usage 

Creating and updating headers accept POST requests with a name of "header" and body containing a list of the 15 required
headers.

*Older forms that did not contain pronouns will still work.*

An example request may resemble:

```
curl --location --request POST 'http://localhost:8080/api/headers/update' \
        --header 'Content-Type: application/json' \
        --data-raw '{
            "name":"header",
            "csvHeaders":[
                "What is your name? (first and last)",
                "",
                "Email Address",
                "Timestamp",
                "How many YEARS have you been a student at UW, including this year? (for example, a sophomore would enter \"2\")",
                "How many QUARTERS have you been in LUX Film Production Club, including this one? (for example, a new LUX member would enter \"1\")",
                "First choice in production:",
                "Second choice in production:",
                "Third choice in production:",
                "First choice in role:",
                "Second choice in role:",
                "Third choice in role:",
                "Would you rather have your preferred ROLE or your preferred PRODUCTION?",
                "Are you interested in acting (and not being on a production crew)?",
                "Which productions would you like to audition for?"
            ]
}
```

It is recommended to use a software like Postman to simplify sending these requests. 

#### Internal Function
 * The database stores only one header entity.
 * Upon receiving a request to upload a CSV, Crew Match retrieves the present headers and performs checks to identify  
 whether the present headers are appropriate for the uploaded CSV.
 * If the headers are not appropriate, an error will be thrown with a message asking for the headers to be updated. 

#### Flow for Usage
 * It is recommended to check and update the headers prior to each CSV file upload to avoid errors. 
 * No internal functionality is damaged by accidentally using the wrong headers, the application will notify  
 if updates are required.

#### Required Headers 

The following 15 headers are required to correctly instantiate candidate entities. The order they are entered in is *critical*.

The examples below are pulled from the 22au Role Interest Survey (Aside from the pronoun field, which is new for 23wi).

| Candidate Property | Example Headers | 
|:------------------:| --------------- |
| Name | What is your name? (first and last) |
| Pronouns | What are your pronouns? |
| Email Address | Email Address |
| Timestamp | Timestamp |
| Years in LUX | How many YEARS have you been a student at UW, including this year? <br> (for example, a sophomore would enter 2) |
| Quarters in LUX | How many QUARTERS have you been in LUX Film Production Club, including this one? <br> (for example, a new LUX member would enter 1) |
| First Choice in Production | First choice in production: |
| Second Choice in Production | Second choice in production: |
| Third Choice in Production | Third choice in production: |
| First Choice in Role | First choice in role: |
| Second Choice in Role | Second choice in role: |
| Third Choice in Role | Third choice in role: |
| Production Preference | Would you rather have your preferred ROLE or your preferred PRODUCTION? |
| Acting Interest | Are you interested in acting (and not being on a production crew)? |
| Productions to Audition For | Which productions would you like to audition for? |


---

### Upcoming functionality changes
  * Ability for productions to be flagged as independent and be exempted from crew matching.
  * ~~Roles controller to get default roles and find which roles need to be included in the Role Interest Form.~~ Implemented in ProductionsController.
