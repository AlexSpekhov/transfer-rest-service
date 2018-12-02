#REST API for transfer-rest-service

Method 	Description
POST /accounts 	Creates new account
POST /accounts?amount=:amount 	Creates new account with money
GET /accounts 	Returns all accounts
GET /accounts/{id} 	Returns account by id
GET /accounts/{id}/balance Returns balance of account by id
DELETE /accounts/{id} 	Closes account by id
GET /accounts/{id}/transfers 	Returns transfers of account by id
POST /accounts/{senderId}/transfers/{receiverId} 	Transfers from account with {senderId} to account with {receiverId}
GET /accounts/{id}/transfers/date?from=:from&to=t 	Returns transfers of account by id with filter by date applied
