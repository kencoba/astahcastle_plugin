package coba.ken.astahcastle

import javax.swing.JOptionPane

import com.change_vision.jude.api.inf.AstahAPI
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException
import com.change_vision.jude.api.inf.project.ProjectAccessor
import com.change_vision.jude.api.inf.ui.IPluginActionDelegate
import com.change_vision.jude.api.inf.ui.IWindow

class TemplateAction extends IPluginActionDelegate {
  @throws(classOf[IPluginActionDelegate.UnExpectedException])
  override def run(window:IWindow):java.lang.Object = {
    try {
      val api = AstahAPI.getAstahAPI
      val projectAccessor = api.getProjectAccessor
      projectAccessor.getProject
      JOptionPane.showMessageDialog(window.getParent,"Hello")
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
                                      "Unexpected error has occurred.",
                                      "Alert",
                                      JOptionPane.ERROR_MESSAGE)
        throw new IPluginActionDelegate.UnExpectedException
      }
    }
    null
  }
}
