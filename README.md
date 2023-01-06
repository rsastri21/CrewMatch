# LUX CrewMatch

*If you are a LUX Officer or Member wanting to report feedback, please contact Rohan Sastri via Slack.*

This repository contains the backend code for the API layer of the CrewMatch application.
The project is written in Java and built with the Spring Boot Web framework. 

---

## API Endpoints

The API contains requests categorized between two entities: *Candidates* and *Productions*.

`The hosting service has not yet been determined, so API endpoints omit the URL at which they will be accessed.`

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
| `/match` | **GET** | Matches candidates to productions according to preferences |
| `/matchNoPreference` | **GET** | Matches candidates without strictly following preferences |
| `/search`  | **GET** | Searches for productions by name |
| `/create` | **POST** | Creates a new production with parameters specified in request body |
| `/update/{id}` | **PUT** | Updates a production by ID with parameters specified in request body |
| `/swap/{production1}/{member1}/{production2}/{member2}` | **PUT** | Swaps members between two productions | 
| `/delete/{id}` | **DELETE** | Deletes a production by ID |

---

### Upcoming functionality changes
  * Ability for productions to be flagged as independent and be exempted from crew matching.
  * Roles controller to get default roles and find which roles need to be included in the Role Interest Form.
