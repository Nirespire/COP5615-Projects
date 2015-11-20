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

#### References for designing user simulator

http://www.sciencedirect.com/science/article/pii/S0747563211000379

Tracii Ryan, Sophia Xenos, Who uses Facebook? An investigation into the relationship between the Big Five, shyness, narcissism, loneliness, and Facebook usage, Computers in Human Behavior, Volume 27, Issue 5, September 2011, Pages 1658-1664, ISSN 0747-5632, http://dx.doi.org/10.1016/j.chb.2011.02.004.
(http://www.sciencedirect.com/science/article/pii/S0747563211000379)
Keywords: Facebook; Big Five; Personality; Narcissism; Shyness; Loneliness

## Types of personalities
- Extraversion
- Agreeableness
- Conscientiousness
- Neuroticism
- Openness

## Types of behavior
- Active Social contributions
    - Puts of posts, pictures, albums
    - Gets of Posts, pictures, albums, pages
    - Adding friends
    - Liking stuff
- Passive engagement
    - Adding friends
    - Gets of Posts, pictures, albums, pages
- Content Creators/celebrities
    - Represented as page
    - Puts of posts, pictures, albums
