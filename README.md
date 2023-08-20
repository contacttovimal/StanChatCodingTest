### __StanCharteredCodingTest__

#### _CacheImpl.java is basic implementation for Cache Interface provided in requirements_
#### _DeadLineEngineImpl.java is basic implementation of the provided requirements_

### **Project setup : Intellij Java Project**
#### JDK - 11 (OpenJDK - Zulu)

### **Using following External API with**
_#### 1. Lombok  - for POJO Builder_
_#### 2. Log4j   - for loging_
_#### 3. Junit   - Junit 4.11_
_#### 4. Mockito - Other Mockito API for advance testing_
_#### 5. Apache  - Apache Common Collection API (utility API)_
_#### 6. TSID    - Copied Request ID Generation Algo from google  (https://stackoverflow.com/questions/15184820/how-to-generate-unique-positive-long-using-uuid )_

### import pom.xml as java project and build maven.

#### DeadLineEngineTest - cover major junits as per the expectation of the requirements
_#####  1. test case with creating 100k DL Unique request_
_#####  2. test case having total Expiry <  max poll capacity_
_#####  3. test case having total Expiry >  max poll capacity_
_#####  4. test case with expiry and cancellation._

#### CacheImplTest - cover major junits as per the expectation of the requirements
_#####  1. Generate 100 keys in parallel, retrieve 200 times and verify method get(),apply() call accordingly_
_#####  2. generate 100 keys and 200 keys in 2 parallel separate thread, retrieve some keys and verify method get(),apply() call accordingly_ 
_#####  3. check Null key in get() behavior_
_#####  4. check Null value by providing InvalidKey for value generation and verify get(),apply() behavior_


### Git Hub REF : https://github.com/contacttovimal/StanChatCodingTest.git

### CodeAssignment.Docx contains requirements details.