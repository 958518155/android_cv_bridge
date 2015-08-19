/*
 * Copyright (c) 2015, Tal Regev
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Android Sensors Driver nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.ros.android.android_tutorial_cv_bridge;

import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.ros.android.RosActivity;
import org.ros.android.android_tutorial_pubsub.R;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.io.IOException;

import cv_bridge.CvImage;
import sensor_msgs.Image;


/**
 * @author Tal Regev
 */
public class MainActivity extends RosActivity implements NodeMain{

    protected Publisher<Image> imagePublisher;
    protected Subscriber<Image> imageSubscriber;
    protected ConnectedNode node;
    protected static final String TAG = "cv_bridge Tutorial";


    public MainActivity() {
    // The RosActivity constructor configures the notification title and ticker
    // messages.
    super("cv_bridge Tutorial", "cv_bridge Tutorial");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {


    // At this point, the user has already been prompted to either enter the URI
    // of a master to use or to start a master locally.

    // The user can easily use the selected ROS Hostname in the master chooser
    // activity.
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
    nodeConfiguration.setMasterUri(getMasterUri());
    nodeMainExecutor.execute(this, nodeConfiguration);
  }

  @Override
  public GraphName getDefaultNodeName() {
    return GraphName.of("android_tutorial_cv_bridge");
  }

  @Override
  public void onStart(ConnectedNode connectedNode) {
      this.node = connectedNode;
      final org.apache.commons.logging.Log log = node.getLog();
      imagePublisher = node.newPublisher("/camera/image_raw", Image._TYPE);
      imageSubscriber = node.newSubscriber("/image_converter/output_video", Image._TYPE);
      imageSubscriber.addMessageListener(new MessageListener<Image>() {
          @Override
          public void onNewMessage(Image message) {
              CvImage cvImage;
              try {
                    cvImage = CvImage.toCvCopy(message);
              } catch (Exception e) {
                  log.error("cv_bridge exception: " + e.getMessage());
                  return;
              }
              if (cvImage.image.rows() > 60 && cvImage.image.cols() > 60)
                  Core.circle(cvImage.image, new Point(50, 50), 10, new Scalar(255,0,0));
              try {
                  imagePublisher.publish(cvImage.toImageMsg(imagePublisher.newMessage()));
              } catch (IOException e) {
                  log.error("cv_bridge exception: " + e.getMessage());
              }
          }
      });

      Log.i(TAG, "called onStart");
  }

  @Override
  public void onShutdown(Node node) {

  }

  @Override
  public void onShutdownComplete(Node node) {

  }

  @Override
  public void onError(Node node, Throwable throwable) {

  }
}
