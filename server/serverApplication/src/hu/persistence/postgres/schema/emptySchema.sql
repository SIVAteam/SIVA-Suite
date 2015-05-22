TRUNCATE "user" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "group" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "userToGroup" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "video" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "token" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "client" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "oauthSession" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "sivaPlayerSession" RESTART IDENTITY CASCADE;/*eoq*/
TRUNCATE "sivaPlayerLog" RESTART IDENTITY CASCADE;/*eoq*/

DROP VIEW "sivaPlayerLogByScene";/*eoq*/
DROP VIEW "sivaPlayerLogMakroNavigation";/*eoq*/
DROP VIEW "sivaPlayerLogVideoNavigation";/*eoq*/
DROP VIEW "sivaPlayerLogAnnotation";/*eoq*/
DROP VIEW "sivaPlayerSessionDurationByClientTime";/*eoq*/
DROP VIEW "sivaPlayerSessionDurationByDayAndUser";/*eoq*/