# Copyright (C) 2009, Progress Software Corporation and/or its 
# subsidiaries or affiliates.  All rights reserved.
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#     http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

$LOAD_PATH << File.dirname(__FILE__)
load 'fuse/asciidoc.rb'
#load 'fuse/sitecopy_rake.rb'

config = Webgen::WebsiteAccess.website.config
config['contentprocessor.map']['asciidoc'] = 'Fuse::AsciiDoc'

module Fuse 
  autoload :AsciiDoc, 'fuse/asciidoc'
  #autoload :SitecopyTask, 'fuse/sitecopy_rake'


end

# Custom HAML filter which is like the plain filter, 
# but which also does not indent the block.
#
require 'haml'
module Haml::Filters::Raw
  include Haml::Filters::Base
  def render_with_options(text, options) 
    options[:ugly]=true
    text
  end
end
