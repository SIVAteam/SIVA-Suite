/* Drop existing schema */
DROP TABLE IF EXISTS "user" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "group" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "userToGroup" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "video" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "token" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "client" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "client" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "oauthSession" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "sivaPlayerSession" CASCADE;/*eoq*/
DROP TABLE IF EXISTS "sivaPlayerLog" CASCADE;/*eoq*/

DROP TYPE IF EXISTS "enumGender" CASCADE;/*eoq*/
DROP TYPE IF EXISTS "enumUserType" CASCADE;/*eoq*/
DROP TYPE IF EXISTS "enumUserGroupRole" CASCADE;/*eoq*/
DROP TYPE IF EXISTS "enumVideoRestriction" CASCADE;/*eoq*/
DROP TYPE IF EXISTS "enumTokenType" CASCADE;/*eoq*/

DROP VIEW IF EXISTS	"sivaPlayerLogByScene" CASCADE;/*eoq*/

/* Triggers are deleted through drop table ... cascade. */

DROP FUNCTION IF EXISTS trigfuncUserInsert() CASCADE;/*eoq*/
DROP FUNCTION IF EXISTS trigfuncUserUpdate() CASCADE;/*eoq*/
DROP FUNCTION IF EXISTS trigfuncUserDelete() CASCADE;/*eoq*/
DROP FUNCTION IF EXISTS trigfuncUserToGroupWrite() CASCADE;/*eoq*/
DROP FUNCTION IF EXISTS trigfuncVideoInsert() CASCADE;/*eoq*/
DROP FUNCTION IF EXISTS trigfuncSivaPlayerLogInsert() CASCADE;/*eoq*/