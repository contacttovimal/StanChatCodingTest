# StanChatCodingTest

# CacheImpl.java is basic implementation for Cache Interface provided in requirements
# DeadLineEngineImpl.java is basic implementation of the provided requirements

#Project setup : Intellij Java Project
 JDK - 11 (OpenJDK - Zulu)

## Using following External API with
# 1. Lombok  - for POJO Builder
# 2. Log4j   - for loging
# 3. Junit   - Junit 4.11
# 4. Mockito - Other Mockito API for advance testing
# 5. Apache  - Apache Common Collection API (utility API)
# 6. TSID    - Copied Request ID Generation Algo from google  (https://stackoverflow.com/questions/15184820/how-to-generate-unique-positive-long-using-uuid )

# import pom.xml as java project and build maven.

## DeadLineEngineTest - cover major junits as per the expectation of the requirements
#  1. test case with creating 100k DL Unique request
#  2. test case having total Expiry <  max poll capacity
#  3. test case having total Expiry >  max poll capacity
#  4. test case with expiry and cancellation.