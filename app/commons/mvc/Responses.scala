package commons.mvc

/**
  * Created by tkhan on 3/15/16.
  */

  object SetValue extends CommandWithKeyData("setValue")


  object ProcessData extends CommandWithData("")

  //only when theres an authentication exception
  object NoAuthFound extends CommandWithData("noAuthFound")

  object AuthTimedOut extends CommandWithData("authTimedOut")

  object RedirectToComponent extends CommandWithKeyData("redirectToComponent")
