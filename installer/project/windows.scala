import sbt._
import Keys._
import com.typesafe.packager.Keys._
import sbt.Keys._
import com.typesafe.packager.PackagerPlugin._
import collection.mutable.ArrayBuffer


trait WindowsPackaging {

  def generateWindowsXml(version: String, dir: File, winDir: File): scala.xml.Node = {
    import com.typesafe.packager.windows.WixHelper._
    val (binIds, binDirXml) = generateComponentsAndDirectoryXml(dir / "bin", "bin_")
    val (srcIds, srcDirXml) = generateComponentsAndDirectoryXml(dir / "src", "src_")
    val (libIds, libDirXml) = generateComponentsAndDirectoryXml(dir / "lib")
    val (miscIds, miscDirXml) = generateComponentsAndDirectoryXml(dir / "misc")
    val docdir = dir / "doc"
    val develdocdir = docdir / "scala-devel-docs"
    val (apiIds, apiDirXml) = generateComponentsAndDirectoryXml(develdocdir / "api", "api_")
    val (exampleIds, exampleDirXml) = generateComponentsAndDirectoryXml(develdocdir / "examples", "ex_")
    val (tooldocIds, tooldocDirXml) = generateComponentsAndDirectoryXml(develdocdir / "tools", "tools_")
    
    (<Wix xmlns='http://schemas.microsoft.com/wix/2006/wi'>
     <Product Id='7606e6da-e168-42b5-8345-b08bf774cb30' 
            Name='The Scala Programming Language' 
            Language='1033'
            Version={version}
            Manufacturer='LAMP/EPFL and Typesafe, Inc.' 
            UpgradeCode='6061c134-67c7-4fb2-aff5-32b01a186968'>
      <Package Description='Scala Programming Language.'
                Comments='Scala Progamming language for use in Windows.'
                Manufacturer='LAMP/EPFL and Typesafe, Inc.' 
                InstallerVersion='200' 
                Compressed='yes'/>
 
      <Media Id='1' Cabinet='scala.cab' EmbedCab='yes' />
 
      <Directory Id='TARGETDIR' Name='SourceDir'>
        <Directory Id='ProgramFilesFolder' Name='PFiles'>
          <Directory Id='INSTALLDIR' Name='scala'>
            <Directory Id='bindir' Name='bin'>
                { binDirXml }  
              <Component Id='ScalaBinPath' Guid='244b8829-bd74-40ff-8c1d-5717be94538d'>
                  <CreateFolder/>
                  <Environment Id="PATH" Name="PATH" Value="[INSTALLDIR]\bin" Permanent="no" Part="last" Action="set" System="yes" />
               </Component>
            </Directory>
            {libDirXml}
            {miscDirXml}
            <Directory Id='srcdir' Name='src'>
              { srcDirXml }
            </Directory>
            <Directory Id='docdir' Name='doc'>
              <!-- TODO - README -->
              <Directory Id='devel_docs_dir' Name='devel-docs'>
                {apiDirXml}
                {exampleDirXml}
                {tooldocDirXml}
              </Directory>
            </Directory>
          </Directory>
         </Directory>
      </Directory>
      
      <Feature Id='Complete' Title='The Scala Programming Language' Description='The windows installation of the Scala Programming Language'
         Display='expand' Level='1' ConfigurableDirectory='INSTALLDIR'>
        <Feature Id='lang' Title='The core scala language.' Level='1' Absent='disallow'>
          { for(ref <- (binIds ++ libIds ++ miscIds)) yield <ComponentRef Id={ref}/> }
        </Feature>
         <Feature Id='ScalaPathF' Title='Update system PATH' Description='This will add scala binaries (scala, scalac, scaladoc, scalap) to your windows system path.' Level='1'>
          <ComponentRef Id='ScalaBinPath'/>
        </Feature>
        <Feature Id='fdocs' Title='Documentation for the Scala library' Description='This will install the Scala documentation.' Level='1'>
          <Feature Id='fapi' Title='API Documentation' Description='Scaladoc API html.' Level='1'>
            { for(ref <- apiIds) yield <ComponentRef Id={ref}/> }
          </Feature>
          <Feature Id='ftooldoc' Title='Tool documentation' Description='Manuals for scala, scalac, scaladoc, etc.' Level='1'>
            { for(ref <- tooldocIds) yield <ComponentRef Id={ref}/> }
          </Feature>
          <Feature Id='fexample' Title='Example Code' Description='Scala code examples.' Level='100'>
            { for(ref <- exampleIds) yield <ComponentRef Id={ref}/> }
          </Feature>
        </Feature>
        <Feature Id='fsrc' Title='Sources' Description='This will install the Scala source files for the binaries.' Level='100'>
            { for(ref <- srcIds) yield <ComponentRef Id={ref}/> }
        </Feature>
      </Feature>
      <MajorUpgrade 
         AllowDowngrades="no" 
         Schedule="afterInstallInitialize"
         DowngradeErrorMessage="A later version of [ProductName] is already installed.  Setup will no exit."/>  
      <UIRef Id="WixUI_FeatureTree"/>
      <UIRef Id="WixUI_ErrorProgressText"/>
      <Property Id="WIXUI_INSTALLDIR" Value="INSTALLDIR"/>
      <WixVariable Id="WixUILicenseRtf" Value={(winDir / "License.rtf").getAbsolutePath } />      
   </Product>
    </Wix>)
  }
}