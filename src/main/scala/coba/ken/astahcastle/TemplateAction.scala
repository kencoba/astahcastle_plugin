package coba.ken.astahcastle

import java.io.File

import javax.swing.JOptionPane
import javax.swing.JFileChooser

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException
import com.change_vision.jude.api.inf.project.ProjectAccessor
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate
import com.change_vision.jude.api.inf.ui.IWindow

import org.slf4j.LoggerFactory

/**
 *
 * astah* plugin for Import Visual Studio XML Document.
 *
 * Before run this plugin, close property window on astah. 
 *
 * @author Kenichi Kobayashi
 */

class TemplateAction extends IPluginActionDelegate {
  private val logger = LoggerFactory.getLogger(classOf[TemplateAction])

  @throws(classOf[IPluginActionDelegate.UnExpectedException])
  override def run(window:IWindow):java.lang.Object = {
    try {
      val api = AstahAPI.getAstahAPI
      val prjAccessor = api.getProjectAccessor
      prjAccessor.getProject

      val filechooser = new JFileChooser
    
      val file = filechooser.showOpenDialog(window.getParent) match {
        case JFileChooser.APPROVE_OPTION => filechooser.getSelectedFile
        case JFileChooser.CANCEL_OPTION => null
        case JFileChooser.ERROR_OPTION => throw new IPluginActionDelegate.UnExpectedException
      }

      XMLDocumentImportUtil.importComment(file,prjAccessor)
    } catch {
      case e:ProjectNotFoundException => {
        val message = "Project is not opened.Please open the project or create new project."
        JOptionPane.showMessageDialog(window.getParent,
                                      message,
                                      "Warning",
                                      JOptionPane.WARNING_MESSAGE)
      }
      case e:Exception => {
        JOptionPane.showMessageDialog(window.getParent,
                                      "Unexpected error has occurred.:" + e.getMessage,
                                      "Alert",
                                      JOptionPane.ERROR_MESSAGE)
        throw new IPluginActionDelegate.UnExpectedException
      }
    }
    null
  }
}
