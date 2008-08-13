/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/

package uk.ac.lancs.e_science.sakaiproject.api.blogger.util;

import java.security.SecureRandom;

/**
 Makes a unique identfier of 22 digits. This identifier is done with the current time of the system,
 a hash code generated by hashCode() method of a instance and a random number. This three values concatenated 
 are converted to hexadecimal.
 
 If it is necesary highger ensure of unicy in identifiers, we can add the IP of the machine that is making the identifier

 */
public class UIDGenerator {
   static public String getIdentifier(Object aInstance) {

        SecureRandom seed = new SecureRandom();
        int node = seed.nextInt();
        String ramdomPart = Integer.toHexString(node);
        
        String hashPart = Integer.toHexString(System.identityHashCode(aInstance));

        long timeNow = System.currentTimeMillis();
        int timeLow = (int) timeNow & 0xFFFFFFFF;
        String timePart = Integer.toHexString(timeLow);

        return timePart + hashPart + ramdomPart;
    }


}