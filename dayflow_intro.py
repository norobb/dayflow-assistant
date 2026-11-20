import bpy
import math

# Clear existing objects
bpy.ops.object.select_all(action='SELECT')
bpy.ops.object.delete()

# Colors
COLOR_BG = (0.96, 0.94, 0.90, 1.0) # #F5EFE6
COLOR_PRIMARY = (0.39, 0.11, 0.14, 1.0) # #641C24
COLOR_CARD = (0.98, 0.96, 0.94, 1.0) # #FAF6F0
COLOR_TEXT = (0.12, 0.11, 0.10, 1.0) # #1E1B19

# Materials
def create_material(name, color, emit=0):
    mat = bpy.data.materials.new(name=name)
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    nodes.clear()

    node_principled = nodes.new(type='ShaderNodeBsdfPrincipled')
    node_principled.inputs['Base Color'].default_value = color
    node_principled.inputs['Roughness'].default_value = 0.3
    if emit > 0:
        node_principled.inputs['Emission Color'].default_value = color
        node_principled.inputs['Emission Strength'].default_value = emit

    node_output = nodes.new(type='ShaderNodeOutputMaterial')
    mat.node_tree.links.new(node_principled.outputs['BSDF'], node_output.inputs['Surface'])
    return mat

mat_primary = create_material("Primary", COLOR_PRIMARY)
mat_card = create_material("Card", COLOR_CARD)
mat_text = create_material("Text", COLOR_TEXT)
mat_screen = create_material("Screen", COLOR_CARD, emit=0.1)

# Create Phone
bpy.ops.mesh.primitive_cube_add(size=1, scale=(0.7, 1.4, 0.05))
phone = bpy.context.active_object
phone.name = "PhoneBody"
phone.data.materials.append(mat_primary)

# Bezel (simplified)
bpy.ops.mesh.primitive_cube_add(size=1, scale=(0.65, 1.35, 0.01))
screen = bpy.context.active_object
screen.name = "PhoneScreen"
screen.location.z = 0.03
screen.data.materials.append(mat_screen)

# Parent screen to phone
screen.parent = phone

# Create floating cards (Schedule, Task, Reminder)
cards = []
card_labels = ["Schedule", "Task", "Reminder"]
for i, label in enumerate(card_labels):
    bpy.ops.mesh.primitive_cube_add(size=1, scale=(0.4, 0.2, 0.01))
    card = bpy.context.active_object
    card.name = f"Card_{label}"
    card.location = (0.8 if i % 2 == 0 else -0.8, -0.5 + i * 0.5, 0.2)
    card.data.materials.append(mat_card)
    cards.append(card)

    # Add a small strip for the color
    bpy.ops.mesh.primitive_cube_add(size=1, scale=(0.02, 0.15, 0.005))
    strip = bpy.context.active_object
    strip.location = (card.location.x - 0.35, card.location.y, card.location.z + 0.01)
    strip.data.materials.append(mat_primary)
    strip.parent = card

# Lighting
bpy.ops.object.light_add(type='AREA', location=(5, -5, 5))
light = bpy.context.active_object
light.data.energy = 1000
light.data.size = 5

bpy.ops.object.light_add(type='AREA', location=(-5, -5, 5))
light2 = bpy.context.active_object
light2.data.energy = 500
light2.data.size = 5

# Camera
bpy.ops.object.camera_add(location=(0, -6, 2))
cam = bpy.context.active_object
cam.rotation_euler = (math.radians(75), 0, 0)
bpy.context.scene.camera = cam

# Animation
bpy.context.scene.frame_start = 1
bpy.context.scene.frame_end = 1500 # Approximately 60 seconds at 25 fps
bpy.context.scene.render.fps = 25

# Animate phone rotation
phone.keyframe_insert(data_path="rotation_euler", frame=1)
phone.rotation_euler.z = math.radians(360) * 5 # Rotate 5 times over the animation
phone.keyframe_insert(data_path="rotation_euler", frame=1500)

# Animate cards floating
for i, card in enumerate(cards):
    card.keyframe_insert(data_path="location", frame=1)
    card.location.z += 0.2
    card.keyframe_insert(data_path="location", frame=int(1500 * 0.5))
    card.location.z -= 0.2
    card.keyframe_insert(data_path="location", frame=1500)

# Render settings
bpy.context.scene.render.resolution_x = 1280
bpy.context.scene.render.resolution_y = 720
bpy.context.scene.render.filepath = "/tmp/dayflow_intro.mp4"
bpy.context.scene.render.image_settings.file_format = 'FFMPEG'
bpy.context.scene.render.ffmpeg.format = 'MPEG4'
bpy.context.scene.render.ffmpeg.codec = 'H264'
bpy.context.scene.render.ffmpeg.constant_rate_factor = 'HIGH'
bpy.context.scene.render.image_settings.color_mode = 'RGB'
bpy.context.scene.render.image_settings.depth = '8'
bpy.context.scene.render.image_settings.compression = 90

# World settings
bpy.context.scene.world.use_nodes = True
bpy.context.scene.world.node_tree.nodes['Background'].inputs['Color'].default_value = COLOR_BG

# Render animation
bpy.ops.render.render(animation=True)

# Final Save
bpy.ops.wm.save_as_mainfile(filepath="/home/norobb/Projects/dayflow-assistant/dayflow_intro.blend")

print("Blender scene created and saved to /home/norobb/Projects/dayflow-assistant/dayflow_intro.blend")
