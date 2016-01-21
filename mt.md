# Monad transformers notes

Because we dont provide json api, yet. We have return Future[Result]
from every action and we cant use implict transformation of any A to
Ok(Result).
But, we can still transform any service result into Future[Xor[Result, A]],
where Result is our error handling Result and A is either Redirect with reverse
route or Ok with HTML template.
We should try to find more specific type than A.
