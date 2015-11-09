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

- Page: https://developers.facebook.com/docs/graph-api/reference/v2.5/page
    /page/{page-id}
- Post: https://developers.facebook.com/docs/graph-api/reference/v2.5/post
    /post/{post-id}
- Friend List: https://developers.facebook.com/docs/graph-api/reference/v2.5/friendlist
    /friendlist/{friendlist-id}
- Profile: https://developers.facebook.com/docs/graph-api/reference/v2.5/profile
    /profile/{profile-id}
- Album: https://developers.facebook.com/docs/graph-api/reference/v2.5/album
    /album/{album-id}
- Picture: https://developers.facebook.com/docs/graph-api/reference/v2.5/album/picture
    /album/{album-id}/{picture-id}

For each:
Create -> PUT
Read -> GET
Update -> POST
Delete -> DELETE
