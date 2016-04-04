package bananja.drunkr.it

/**
  * Created by tashdidkhan on 3/17/16.
  */
import bananja.drunkr.AppApplicationLoader
import play.api.test.WithApplicationLoader

class IntegrationContext extends WithApplicationLoader(
  applicationLoader = new AppApplicationLoader
)
