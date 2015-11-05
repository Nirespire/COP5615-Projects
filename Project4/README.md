# Project 4

## Contributors

1. Sanjay Nair
2. Preethu Thomas


## Overview


 
## What is Working


## How to Run


## Implementation Details


#### Overall Architecture

- Client simulator
    - Need to emulate different types of users
    - Different percentages need to
        - post a lot
        - read a lot
        - don't do much
    - Need ~100k simulated clients min
    1. Login/sign up?
    2. Send friend requests?
    3. Make posts
    4. Read posts

- Facebook HTTP server
    - Spray-Can based
    - Need in-memory data structures to hold all fb info
    - Need to be able to communicate via HTTP in JSON

#### Facebook Graph API components
    
https://developers.facebook.com/docs/graph-api/reference/v2.5/profile
- Profile -> abstract
    - User
    - Page
    - Group
    - Event
    - Application

https://developers.facebook.com/docs/graph-api/reference/user
- User
    - id
    - about
    - birthday
    - first_name
    - last_name
    - gender
    
https://developers.facebook.com/docs/graph-api/reference/v2.5/post
- Post
   - id
   - from
   - message
   - created_time
   
- Publish by POST to /{user-id}/feed, /{page-id}/feed, /{event-id}/feed, or /{group-id}/feed
    
    
https://developers.facebook.com/docs/graph-api/reference/v2.5/comment
- Comment
    - id
    - created_time
    - message
    - like_count
    - user_likes
- Edit via POST to /{comment-id} message=""
- Delete via DELETE to /{comment-id}
- Get all comments of an object via /{object-id}/comments
- Publish via POST to /{object-id}/comments message="" -> returns created comment id
